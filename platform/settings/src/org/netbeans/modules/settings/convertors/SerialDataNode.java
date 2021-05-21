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

package org.netbeans.modules.settings.convertors;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextProxy;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.actions.ToolsAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.BeanChildren;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/** Node to represent a .settings file.
 * @author Jan Pokorsky
 */
public final class SerialDataNode extends DataNode {
    /** listener for properties */
    private PropL propertyChangeListener = null;
    /** bean info is not used only if the file specifies 
     * <attr name="beaninfo" booleanvalue="false" />
     */
    private boolean noBeanInfo = false;
    private final SerialDataConvertor convertor;
    private WeakReference<Object> settingInstance = new WeakReference<Object>(null);
    private boolean isNameChanged = false;
    /** true after passing the constructors code
     * @see #getName
     */
    private final Boolean isAfterNodeConstruction;

    static final String ATTR_DISPLAY_NAME = "SerialDataNode_DisplayName";//NOI18N
            
    /** used by general setting objects */
    public SerialDataNode(DataObject dobj) {
        this(null, dobj,  Boolean.FALSE.equals(dobj.
            getPrimaryFile().getAttribute("beaninfo"))); // NOI18N
    }
    
    /** used by old serialdata settings */
    public SerialDataNode(SerialDataConvertor conv) {
        this (conv, conv.getDataObject(), Boolean.FALSE.equals(conv.getDataObject().
            getPrimaryFile().getAttribute("beaninfo"))); // NOI18N
    }
     
    /** @param obj the object to use
     * @param noBeanInfo info to use
     */
    private SerialDataNode(SerialDataConvertor conv, DataObject dobj, boolean noBeanInfo) {
        super (dobj, getChildren(dobj, noBeanInfo));
        
        this.convertor = conv;
        this.noBeanInfo = noBeanInfo;
        isAfterNodeConstruction = Boolean.TRUE;
    }
    
    private static Children getChildren(DataObject dobj, boolean noBeanInfo) {
        if (noBeanInfo) {
            return Children.LEAF;
        }
        InstanceCookie inst = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
        if (inst == null) return Children.LEAF;
        try {
            Class<?> clazz = inst.instanceClass();
            if (BeanContext.class.isAssignableFrom(clazz) ||
                BeanContextProxy.class.isAssignableFrom(clazz)) {
                return new InstanceChildren ();
            } else {
                return Children.LEAF;
            }
        } catch (Exception ex) {
            return Children.LEAF;
        }
    }
    
    /** Getter for instance data object.
     * @return instance data object
     */
    private InstanceDataObject i () {
        return (InstanceDataObject)getDataObject ();
    }
    
    private InstanceCookie.Of ic () {
        return (InstanceCookie.Of) getDataObject().getCookie(InstanceCookie.Of.class);
    }
    
    private SerialDataConvertor getConvertor() {
        return convertor;
    }
    
    /** get cached setting object; can be null */
    private Object getSettingInstance() {
        return settingInstance.get();
    }
    /** cache setting object */
    private void setSettingsInstance(Object obj) {
        if (obj == settingInstance.get()) return;
        isNameChanged = false;
        settingInstance = new WeakReference<Object>(obj);
    }
    
    /** Find an icon for this node (in the closed state).
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use to represent the node
    */
    public @Override Image getIcon (int type) {
        if (noBeanInfo) {
            return super.getIcon(type);
        }
        Image img = initIcon(type);
        if (img == null) {
            img = super.getIcon(type);
        }
        try {
            DataObject dobj = getDataObject();
            return FileUIUtils.getImageDecorator(dobj.getPrimaryFile().getFileSystem()).
                    annotateIcon(img, type, dobj.files());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
            return img;
        }
    }

    /** Find an icon for this node (in the open state).
    * This icon is used when the node may have children and is expanded.
    *
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use to represent the node when open
    */
    public Image getOpenedIcon (int type) {
        return getIcon (type);
    }
    
    public String getHtmlDisplayName() {
        return null;
    }
    
    /** here should be decided if some change was fired by the setting object
     * or the node should notify convertor about the change. This is just
     * workaround ensuring backward compatibility for archaic settings from
     * the "store everything at shutdown" ages and newly writtten setting should
     * not rely on it.
     */
    private void resolvePropertyChange() {
        if (propertyChangeListener != null &&
            propertyChangeListener.getChangeAndReset()) return;
        
        if (notifyResolvePropertyChange && propertyChangeListener != null ) {
            notifyResolvePropertyChange = false;
            XMLSettingsSupport.err.warning(
                "Warning: no PropertyChangeEvent fired from settings stored in " +// NOI18N
                getDataObject());
        }
        SerialDataConvertor c = getConvertor();
        if (c != null) {
            c.handleUnfiredChange();
        }
    }
    
    private boolean notifyResolvePropertyChange = true;
        

    /** try to register PropertyChangeListener to instance to fire its changes.
     * @param bean     */
    private void initPList (Object bean, BeanInfo bInfo, BeanNode.Descriptor descr) {
        EventSetDescriptor[] descs  = bInfo.getEventSetDescriptors();
        try {
            Method setter = null;
            for (int i = 0; descs != null && i < descs.length; i++) {
                setter = descs[i].getAddListenerMethod();
                if (setter != null && setter.getName().equals("addPropertyChangeListener")) { // NOI18N                    
                    propertyChangeListener = new PropL(createSupportedPropertyNames(descr));
                    setter.invoke(bean, new Object[] {WeakListeners.propertyChange(propertyChangeListener, bean)});
                    setSettingsInstance(bean);
                }
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    private Collection<String> createSupportedPropertyNames(BeanNode.Descriptor descr) {
        ArrayList<String> supportedPropertyNames = new ArrayList<String>();
        if (descr.property != null) {
            for (int i = 0; i < descr.property.length; i++) {
                Node.Property property = descr.property[i];
                supportedPropertyNames.add(property.getName());
            }
        }

        if (descr.expert != null) {
            for (int i = 0; i < descr.expert.length; i++) {
                Property property = descr.property[i];
                supportedPropertyNames.add(property.getName());
            }
        }

        return supportedPropertyNames;
    }

    private Image initIcon (int type) {
        Image beanInfoIcon = null;
        try {
            InstanceCookie ic = ic();
            if (ic == null) return null;
            Class<?> clazz = ic.instanceClass();
            //Fixed bug #5610
            //Class javax.swing.JToolBar$Separator does not have icon
            //we will use temporarily icon from javax.swing.JSeparator
            //New icon is requested.

            String className = clazz.getName ();
            BeanInfo bi;
            if (
                className.equals ("javax.swing.JSeparator") ||  // NOI18N
                className.equals ("javax.swing.JToolBar$Separator") // NOI18N
            ) {
                Class clazzTmp = Class.forName ("javax.swing.JSeparator"); // NOI18N
                bi = Utilities.getBeanInfo (clazzTmp);
            } else {
                bi = Utilities.getBeanInfo (clazz);
            }

            if (bi != null) {
                beanInfoIcon = bi.getIcon (type);
            }
            // Also specially handle SystemAction's.
            if (SystemAction.class.isAssignableFrom (clazz)) {
                SystemAction action = SystemAction.get (clazz.asSubclass(SystemAction.class));
                if (beanInfoIcon == null) {
                    Icon icon = action.getIcon ();
                    if (icon != null) {
                        beanInfoIcon = ImageUtilities.icon2Image(icon);
                    }
                }
            }
        } catch (Exception e) {
            // Problem ==>> use default icon
            Logger.getLogger(SerialDataNode.class.getName()).log(Level.WARNING, null, e);
        }

        return beanInfoIcon;
    }

    /** Try to get display name of the bean.
     */
    private String getNameForBean() {
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                // it must be unrecognized setting
                return NbBundle.getMessage(SerialDataNode.class,
                    "LBL_BrokenSettings"); //NOI18N
            }
            Class<?> clazz = ic.instanceClass();
            Method nameGetter;
            Class[] param = new Class [0];
            try {
                nameGetter = clazz.getMethod ("getName", param); // NOI18N
                if (nameGetter.getReturnType () != String.class) throw new NoSuchMethodException ();
            } catch (NoSuchMethodException e) {
                try {
                    nameGetter = clazz.getMethod ("getDisplayName", param); // NOI18N
                    if (nameGetter.getReturnType () != String.class) throw new NoSuchMethodException ();
                } catch (NoSuchMethodException ee) {
                    return null;
                }
            }
            Object bean = ic.instanceCreate();
            setSettingsInstance(bean);
            return (String) nameGetter.invoke (bean);
        } catch (Exception ex) {
            // ignore
            return null;
        }
    }
    
    /** try to find setter setName/setDisplayName, if none declared return null */
    private Method getDeclaredSetter() {
        Method nameSetter = null;
        try {
            InstanceCookie ic = ic();
            if (ic == null) return null;
            Class<?> clazz = ic.instanceClass();
            Class[] param = new Class[] {String.class};
            // find the setter for the name
            try {
                nameSetter = clazz.getMethod ("setName", param); // NOI18N
            } catch (NoSuchMethodException e) {
                nameSetter = clazz.getMethod ("setDisplayName", param); // NOI18N
            }
            if (!Modifier.isPublic(nameSetter.getModifiers())) {
                nameSetter = null;
            }
        } catch (Exception ex) {
            // ignore
        }
        return nameSetter;
    }
    
    public void setName(String name) {
        String old = getName();
        if (old != null && old.equals(name)) return;
        InstanceCookie ic = ic();
        if (ic == null) {
            return;
        }
        
        Method nameSetter = getDeclaredSetter();
        if (nameSetter != null) {
            try {
                Object bean = ic.instanceCreate();
                setSettingsInstance(bean);
                nameSetter.invoke(bean, new Object[] {name});
                isNameChanged = true;
                getDataObject().getPrimaryFile().setAttribute(ATTR_DISPLAY_NAME, name);                
                resolvePropertyChange();
                return;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.attachLocalizedMessage(ex, getDataObject().toString());
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.attachLocalizedMessage(ex, getDataObject().toString());
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.attachLocalizedMessage(ex, getDataObject().toString());
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public String getName() {
        // the fix of #17247; DataNode performs some weird initialization of
        // the name in its constructor so SDN delegates to lazy getDisplayName
        // impl when it is really necessary to prevent useless creating of
        // the setting object.
        if (isAfterNodeConstruction == null) return super.getName();
        return getDisplayName();
    }
    
    /** Get the display name for the node.
     * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
     * @return the desired name
    */
    public String getDisplayName () {
        String name;
        Object setting = getSettingInstance();
        if (setting != null && isNameChanged) {
            // due to async storing ask a bean for name first
            name = getNameForBean();
            if (name != null) {
                return name;
            }
        }
        
        
        name = (String) getDataObject().getPrimaryFile().
            getAttribute(SerialDataNode.ATTR_DISPLAY_NAME);
        if (name == null) {
            try {
                String def = "\b"; // NOI18N
                StatusDecorator fsStatus = getDataObject().getPrimaryFile().
                    getFileSystem().getDecorator();
                name = fsStatus.annotateName(def, getDataObject().files());
                if (name.indexOf(def) < 0) {
                    return name;
                } else {
                    name = getNameForBean();
                    if (name != null) {
                        name = fsStatus.annotateName (name, getDataObject().files());
                    } else {
                        name = fsStatus.annotateName (getDataObject().getName(),
                            getDataObject().files());
                    }
                }
            } catch (FileStateInvalidException e) {
                // no fs, do nothing
            }
        }
        return name;
    }
    
    protected Sheet createSheet () {
        Sheet retVal = new Sheet();
        InstanceCookie ic = ic();

        if (ic != null) {
            
            try {
                Class instanceClass = ic.instanceClass ();
                Object bean = ic.instanceCreate ();
                BeanInfo beanInfo = Utilities.getBeanInfo (instanceClass);
                
                BeanNode.Descriptor descr = BeanNode.computeProperties (bean, beanInfo);
                BeanDescriptor bd = beanInfo.getBeanDescriptor();
                
                initPList(bean, beanInfo, descr);
                
                retVal.put (createPropertiesSet(descr, bd));

                if (descr.expert != null && descr.expert.length != 0) {
                    retVal.put (createExpertSet(descr, bd));
                }                
                
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SerialDataNode.class.getName()).log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SerialDataNode.class.getName()).log(Level.WARNING, null, ex);
            } catch (IntrospectionException ex) {
                Logger.getLogger(SerialDataNode.class.getName()).log(Level.WARNING, null, ex);
            }            
        }

        return retVal;
    }


    private Sheet.Set createExpertSet(BeanNode.Descriptor descr, BeanDescriptor bd) {
        Sheet.Set p = Sheet.createExpertSet();
        convertProps (p, descr.expert, this);
        if (bd != null) {
            Object helpID = bd.getValue("expertHelpID"); // NOI18N
            if (helpID != null && helpID instanceof String) {
                p.setValue("helpID", helpID); // NOI18N
            }
        }
        return p;
    }

    private Sheet.Set createPropertiesSet(BeanNode.Descriptor descr, BeanDescriptor bd) {
        Sheet.Set props;
        props = Sheet.createPropertiesSet();
        if (descr.property != null) {
            convertProps (props, descr.property, this);
        }
        if (bd != null) {
            // #29550: help from the beaninfo on property tabs
            Object helpID = bd.getValue("propertiesHelpID"); // NOI18N
            if (helpID != null && helpID instanceof String) {
                props.setValue("helpID", helpID); // NOI18N
            }
        }
        return props;
    }


    /** Method that converts properties of an object.
     * @param set set to add properties to
     * @param arr array of Node.Property and Node.IndexedProperty
     * @param ido provides task to invoke when a property changes
     */
    private static final void convertProps (
        Sheet.Set set, Node.Property<?>[] arr, SerialDataNode ido
    ) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Node.IndexedProperty) {
                set.put (new I ((Node.IndexedProperty)arr[i], ido));
            } else {
                set.put (new P (arr[i], ido));
            }
        }
    }        
    
    /** Indicate whether the node may be renamed.
     * @return tests {@link DataObject#isRenameAllowed}
     */
    public boolean canRename() {
        return getDeclaredSetter() != null;
    }
    
    /** Indicate whether the node may be destroyed.
     * @return tests {@link DataObject#isDeleteAllowed}
     */
    public boolean canDestroy() {
        try {
            InstanceCookie ic = ic();
            if (ic == null) return true;
            Class<?> clazz = ic.instanceClass();
            return (!SharedClassObject.class.isAssignableFrom(clazz));
        } catch (Exception ex) {
            return true;
        }
    }
    
    public boolean canCut() {
        try {
            InstanceCookie ic = ic();
            if (ic == null) return false;
            Class<?> clazz = ic.instanceClass();
            return (!SharedClassObject.class.isAssignableFrom(clazz));
        } catch (Exception ex) {
            return false;
        }
    }
    
    public boolean canCopy() {
        try {
            InstanceCookie ic = ic();
            if (ic == null) return false;
            Class<?> clazz = ic.instanceClass();
            return (!SharedClassObject.class.isAssignableFrom(clazz));
        } catch (Exception ex) {
            return false;
        }
    }
    
    /** Gets the short description of this feature. */
    public String getShortDescription() {
        if (noBeanInfo) return super.getShortDescription();
        
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                // it must be unrecognized instance
                return getDataObject().getPrimaryFile().toString();
            }
            
            Class<?> clazz = ic.instanceClass();
            BeanDescriptor bd = Utilities.getBeanInfo(clazz).getBeanDescriptor();
            String desc = bd.getShortDescription();
            return (desc.equals(bd.getDisplayName()))? getDisplayName(): desc;
        } catch (Exception ex) {
            return super.getShortDescription();
        }
    }
    
    /* do not want CustomizeBean to be invoked on double-click */
    public Action getPreferredAction() {
        return null;
    }
    
    //
    // inner classes - properties
    //
    
    /** A property that delegates every call to original property
     * but when modified, also starts a saving task.
     */
    private static final class P extends Node.Property {
        /** delegate */
        private Node.Property del;
        /** task to executed */
        private SerialDataNode t;

        public P (Node.Property del, SerialDataNode t) {
            super (del.getValueType ());
            this.del = del;
            this.t = t;
        }

        public void setName(java.lang.String str) {
            del.setName(str);
        }

        public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
            del.restoreDefaultValue();
        }

        public void setValue(String str, Object obj) {
            del.setValue(str, obj);
        }

        public boolean supportsDefaultValue() {
            return del.supportsDefaultValue();
        }

        public boolean canRead() {
            return del.canRead ();
        }

        public PropertyEditor getPropertyEditor() {
            return del.getPropertyEditor();
        }

        public boolean isHidden() {
            return del.isHidden();
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return del.getValue ();
        }

        public void setExpert(boolean param) {
            del.setExpert(param);
        }

        /** Delegates the set value and also saves the bean.
         */
        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            del.setValue (val);
            t.resolvePropertyChange();
        }

        public void setShortDescription(String str) {
            del.setShortDescription(str);
        }

        public boolean isExpert() {
            return del.isExpert();
        }

        public boolean canWrite() {
            return del.canWrite ();
        }

        public Class getValueType() {
            return del.getValueType();
        }

        public String getDisplayName() {
            return del.getDisplayName();
        }

        public Enumeration<String> attributeNames() {
            return del.attributeNames();
        }

        public String getShortDescription() {
            return del.getShortDescription();
        }

        public String getName() {
            return del.getName();
        }

        public void setHidden(boolean param) {
            del.setHidden(param);
        }

        public void setDisplayName(String str) {
            del.setDisplayName(str);
        }

        public boolean isPreferred() {
            return del.isPreferred();
        }

        public Object getValue(String str) {
            return del.getValue(str);
        }

        public void setPreferred(boolean param) {
            del.setPreferred(param);
        }

    } // end of P

    /** A property that delegates every call to original property
     * but when modified, also starts a saving task.
     */
    private static final class I extends Node.IndexedProperty {
        /** delegate */
        private Node.IndexedProperty del;
        /** task to executed */
        private SerialDataNode t;

        public I (Node.IndexedProperty del, SerialDataNode t) {
            super (del.getValueType (), del.getElementType ());
            this.del = del;
            this.t = t;
        }

        public void setName(java.lang.String str) {
            del.setName(str);
        }

        public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
            del.restoreDefaultValue();
        }

        public void setValue(String str, Object obj) {
            del.setValue(str, obj);
        }

        public boolean supportsDefaultValue() {
            return del.supportsDefaultValue();
        }

        public boolean canRead() {
            return del.canRead ();
        }

        public PropertyEditor getPropertyEditor() {
            return del.getPropertyEditor();
        }

        public boolean isHidden() {
            return del.isHidden();
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return del.getValue ();
        }

        public void setExpert(boolean param) {
            del.setExpert(param);
        }

        /** Delegates the set value and also saves the bean.
         */
        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            del.setValue (val);
            t.resolvePropertyChange();
        }

        public void setShortDescription(String str) {
            del.setShortDescription(str);
        }

        public boolean isExpert() {
            return del.isExpert();
        }

        public boolean canWrite() {
            return del.canWrite ();
        }

        public Class getValueType() {
            return del.getValueType();
        }

        public String getDisplayName() {
            return del.getDisplayName();
        }

        public Enumeration<String> attributeNames() {
            return del.attributeNames();
        }

        public String getShortDescription() {
            return del.getShortDescription();
        }

        public String getName() {
            return del.getName();
        }

        public void setHidden(boolean param) {
            del.setHidden(param);
        }

        public void setDisplayName(String str) {
            del.setDisplayName(str);
        }

        public boolean isPreferred() {
            return del.isPreferred();
        }

        public Object getValue(String str) {
            return del.getValue(str);
        }

        public void setPreferred(boolean param) {
            del.setPreferred(param);
        }

        public boolean canIndexedRead () {
            return del.canIndexedRead ();
        }

        public Class getElementType () {
            return del.getElementType ();
        }

        public Object getIndexedValue (int index) throws
        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return del.getIndexedValue (index);
        }

        public boolean canIndexedWrite () {
            return del.canIndexedWrite ();
        }

        public void setIndexedValue (int indx, Object val) throws
        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            del.setIndexedValue (indx, val);
            t.resolvePropertyChange();
        }

        public PropertyEditor getIndexedPropertyEditor () {
            return del.getIndexedPropertyEditor ();
        }
    } // end of I
    
    /** Derived from BeanChildren and allow replace beancontext. */
    private final static class InstanceChildren extends Children.Keys {
        SerialDataNode task;
        DataObject dobj;
        Object bean;
        ContextL contextL = null;
        
        public InstanceChildren() {
        }
        
        protected void addNotify () {
            super.addNotify();
            
            task = (SerialDataNode) getNode();
            dobj = task.getDataObject();
            // attaches a listener to the bean
            contextL = new ContextL (this);
            init();
        }
        
        protected void removeNotify () {
            if (contextL != null && bean != null)
                ((BeanContext) bean).removeBeanContextMembershipListener (contextL);
            contextL = null;
            
            setKeys(Collections.emptySet());
        }
        
        private void init() {
            try {
                InstanceCookie ic = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                if (ic == null) {
                    bean = null;
                    return;
                }
                Class<?> clazz = ic.instanceClass();
                if (BeanContext.class.isAssignableFrom(clazz)) {
                    bean = ic.instanceCreate();
                } else if (BeanContextProxy.class.isAssignableFrom(clazz)) {
                    bean = ((BeanContextProxy) ic.instanceCreate()).getBeanContextProxy();
                } else {
                    bean = null;
                }
            } catch (Exception ex) {
                bean = null;
                Exceptions.printStackTrace(ex);
            }
            if (bean != null) {
                // attaches a listener to the bean
                ((BeanContext) bean).addBeanContextMembershipListener (contextL);
            }
            updateKeys();
        }
        
        private void updateKeys() {
            if (bean == null) {
                setKeys(Collections.emptySet());
            } else {
                setKeys(((BeanContext) bean).toArray());
            }
        }
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            Object ctx = bean;
            if (bean == null) return new Node[0];
            
            try {
                if (key instanceof BeanContextSupport) {
                    BeanContextSupport bcs = (BeanContextSupport)key;

                    if (((BeanContext) ctx).contains (bcs.getBeanContextPeer())) {
                        // sometimes a BeanContextSupport occures in the list of
                        // beans children even there is its peer. we think that
                        // it is desirable to hide the context if the peer is
                        // also present
                        return new Node[0];
                    }
                }

                return new Node[] { new BeanContextNode (key, task) };
            } catch (IntrospectionException ex) {
                // ignore the exception
                return new Node[0];
            }
        }
        
        /** Context listener.
        */
        private static final class ContextL implements BeanContextMembershipListener {
            /** weak reference to the BeanChildren object */
            private final Reference<InstanceChildren> ref;

            /** Constructor */
            ContextL (InstanceChildren bc) {
                ref = new WeakReference<InstanceChildren> (bc);
            }

            /** Listener method that is called when a bean is added to
            * the bean context.
            * @param bcme event describing the action
            */
            public void childrenAdded (BeanContextMembershipEvent bcme) {
                InstanceChildren bc = ref.get ();
                if (bc != null) {
                    bc.updateKeys();
                }
            }

            /** Listener method that is called when a bean is removed to
            * the bean context.
            * @param bcme event describing the action
            */
            public void childrenRemoved (BeanContextMembershipEvent bcme) {
                InstanceChildren bc = ref.get ();
                if (bc != null) {
                    bc.updateKeys ();
                }
            }
        }
    }
    
    /** Creates BeanContextNode for each bean
    */
    private static class BeanFactoryImpl implements BeanChildren.Factory {
        SerialDataNode task;
        public BeanFactoryImpl(SerialDataNode task) {
            this.task = task;
        }
        
        /** @return bean node */
        public Node createNode (Object bean) throws IntrospectionException {
            return new BeanContextNode (bean, task);
        }
    }
    
    private static class BeanContextNode extends BeanNode<Object> {
        public BeanContextNode(Object bean, SerialDataNode task) throws IntrospectionException {
            super(bean, getChildren(bean, task));
            changeSheet(getSheet(), task);
        }
        
        private void changeSheet(Sheet orig, SerialDataNode task) {
            Sheet.Set props = orig.get (Sheet.PROPERTIES);
            if (props != null) {
                convertProps (props, props.getProperties(), task);
            }

            props = orig.get(Sheet.EXPERT);
            if (props != null) {
                convertProps (props, props.getProperties(), task);
            }
        }
        private static Children getChildren (Object bean, SerialDataNode task) {
            if (bean instanceof BeanContext)
                return new BeanChildren ((BeanContext)bean, new BeanFactoryImpl(task));
            if (bean instanceof BeanContextProxy) {
                java.beans.beancontext.BeanContextChild bch = ((BeanContextProxy)bean).getBeanContextProxy();
                if (bch instanceof BeanContext)
                    return new BeanChildren ((BeanContext)bch, new BeanFactoryImpl(task));
            }
            return Children.LEAF;
        }
        
        // #7925
        public boolean canDestroy() {
            return false;
        }

        public Action[] getActions(boolean context) {
            return removeActions(super.getActions(context), new Action[] {SystemAction.get(ToolsAction.class)});
        }
        
        private static Action[] removeActions(Action[] allActions, Action[] toDeleteActions) {
            Action[] retVal = allActions;
            List<Action> actions = Arrays.asList(allActions);
            for (int i = 0; i < toDeleteActions.length; i++) {
                Action a = toDeleteActions[i];
                if(actions.contains(a)) {
                    actions = new ArrayList<Action>(actions); // to be mutable
                    actions.remove(a);
                    retVal = actions.toArray(new Action[0]);
                }                
            }            
            return retVal;
        }        
    }
    
    /** Property change listener to update the properties of the node and
    * also the name of the node (sometimes)
    */
    private final class PropL extends Object implements PropertyChangeListener {
        private Collection<String> supportedPropertyNames;        
        PropL(Collection<String> supportedPropertyNames) {
            this.supportedPropertyNames = supportedPropertyNames;
        }
        
        private boolean isChanged = false;
        public void propertyChange(PropertyChangeEvent e) {
            isChanged = true;
            String name = e.getPropertyName();

            if (isSupportedName(name)) {
                firePropertyChange (name, e.getOldValue (), e.getNewValue ());
            
                if (name.equals("name")) { // NOI18N
                    SerialDataNode.this.isNameChanged = true;
                    // ensure the display name is updated also for ServiceTypes
                    SerialDataNode.this.fireDisplayNameChange(null, null);
                } else if (name.equals("displayName")) { // NOI18N
                    SerialDataNode.this.isNameChanged = true;
                }
            }
        }

        private boolean isSupportedName(String name) {
            return name != null && supportedPropertyNames.contains(name);
        }

        public boolean getChangeAndReset() {
            boolean wasChanged = isChanged;
            isChanged = false;
            return wasChanged;
        }
    }
        
}
