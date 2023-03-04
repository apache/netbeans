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

package org.openide.loaders;


import java.awt.Image;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;

/** Node to represent a .settings, .ser or .instance file.
 *
 * @author  Jaroslav Tulach, Jan Pokorsky
 */
final class InstanceNode extends DataNode implements Runnable {
    
    /** icon base */
    private static final String INSTANCE_ICON_BASE =
        "org/openide/loaders/instanceObject.gif"; // NOI18N
    
    /** File extension for xml settings. */
    private static final String XML_EXT = "settings"; //NOI18N
    
    /** listener for properties */
    private PropL propertyChangeListener = null;
    private PropertyChangeListener dobjListener;
    private boolean isSheetCreated = false;
    /** bean info is not used only if the file specifies 
     * <attr name="beaninfo" booleanvalue="false" />
     */
    private boolean noBeanInfo = false;

    /** Constructor */
    public InstanceNode (InstanceDataObject obj) {
        this (obj,  Boolean.FALSE.equals (obj.getPrimaryFile ().getAttribute ("beaninfo"))); // NOI18N
    }
     
    /** @param obj the object to use
     * @param noBeanInfo info to use
     */
    private InstanceNode (InstanceDataObject obj, boolean noBeanInfo) {
        super (obj, getChildren(obj, noBeanInfo));
        
        initIconBase();

        this.noBeanInfo = noBeanInfo;
        
        if (!noBeanInfo && !getDataObject().getPrimaryFile().hasExt(XML_EXT)) {
            SwingUtilities.invokeLater (this);
        }
        
        // listen on the cookie change of the instance data object
        dobjListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DataObject.PROP_COOKIE)) {
                    if (propertyChangeListener != null) {
                        propertyChangeListener.destroy();
                        propertyChangeListener = null;
                    }
                    if (InstanceNode.this.noBeanInfo || ic() == null) {
                       initIconBase();
                    } else {
                       fireIconChange();
                    }
                    fireNameChange(null, null);
                    fireDisplayNameChange(null, null);
                    fireShortDescriptionChange(null, null);
                    if (isSheetCreated) {
                        setSheet(createSheet());
                    }
                }
            }
        };
        obj.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(dobjListener, obj));
    }
    
    /** initialize the icon base according to state of the settings instance (valid/broken) */
    private void initIconBase() {
        InstanceCookie.Of ic = ic();
        String iconBase = INSTANCE_ICON_BASE;
        if (ic == null) {//XXX && io.instanceOf(XMLSettingsSupport.BrokenSettings.class)) {
            iconBase = "org/openide/loaders/instanceBroken.gif"; // NOI18N
        }
        setIconBaseWithExtension(iconBase);
    }
    
    private static Children getChildren(DataObject dobj, boolean noBeanInfo) {
        if (noBeanInfo) {
            return Children.LEAF;
        }
        InstanceCookie inst = dobj.getCookie(InstanceCookie.class);
        if (inst == null) {
            return Children.LEAF;
        }
        try {
            Class<?> clazz = inst.instanceClass();
            if (BeanContext.class.isAssignableFrom(clazz) ||
                BeanContextProxy.class.isAssignableFrom(clazz)) {
                return new InstanceChildren ((InstanceDataObject) dobj);
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
        return getDataObject().getCookie(InstanceCookie.Of.class);
    }

    /** Find an icon for this node (in the closed state).
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use to represent the node
    */
    @Override
    public Image getIcon (int type) {
        if (noBeanInfo) {
            return super.getIcon(type);
        }
        Image img = null;
        try {
            DataObject dobj = getDataObject();
            img = FileUIUtils.getImageDecorator(dobj.getPrimaryFile().getFileSystem()).
                annotateIcon (img, type, dobj.files ());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }
        
        if (img == null) {
            img = initIcon(type);
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }

    /** Find an icon for this node (in the open state).
    * This icon is used when the node may have children and is expanded.
    *
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use to represent the node when open
    */
    @Override
    public Image getOpenedIcon (int type) {
        return getIcon (type);
    }

    /** try to register PropertyChangeListener to instance to fire its changes.*/
    private void initPList () {
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                return;
            }
            BeanInfo info = Utilities.getBeanInfo(ic.instanceClass());
            java.beans.EventSetDescriptor[] descs = info.getEventSetDescriptors();
            Method setter = null;
            for (int i = 0; descs != null && i < descs.length; i++) {
                setter = descs[i].getAddListenerMethod();
                if (setter != null && setter.getName().equals("addPropertyChangeListener")) { // NOI18N
                    Object bean = ic.instanceCreate();
                    propertyChangeListener = new PropL();
                    setter.invoke(bean, new Object[] {org.openide.util.WeakListeners.propertyChange(propertyChangeListener, bean)});
                }
            }
        } catch (Exception ex) {
        } catch (LinkageError ex) {
            // #30650 - catch also LinkageError.
            // Ignoring exception the same way as the Exception handler above.
        }
    }

    private boolean brokenIcon;
    private Image initIcon (int type) {
        if (brokenIcon) {
            return null;
        }
        Image beanInfoIcon = null;
        InstanceCookie ic = null;
        try {
            ic = ic();
            if (ic == null) {
                return null;
            }
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
                Class<?> clazzTmp = Class.forName ("javax.swing.JSeparator"); // NOI18N
                bi = Utilities.getBeanInfo (clazzTmp);
            } else {
                bi = Utilities.getBeanInfo (clazz);
            }

            if (bi != null) {
                beanInfoIcon = bi.getIcon (type);
                if (beanInfoIcon != null) {
                    beanInfoIcon = toBufferedImage(beanInfoIcon, true);
                }
            }
            // Also specially handle action instances.
            if (beanInfoIcon == null && Action.class.isAssignableFrom(clazz)) {
                Action action = (Action)ic.instanceCreate();
                Icon icon = (Icon)action.getValue(Action.SMALL_ICON);
                if (icon != null) {
                    beanInfoIcon = ImageUtilities.icon2Image(icon);
                } else {
                    Object base = action.getValue("iconBase"); // NOI18N
                    if (base instanceof String) {
                        beanInfoIcon = ImageUtilities.loadImage((String) base, true);
                    } else {
                        beanInfoIcon = ImageUtilities.loadImage("org/openide/loaders/empty.gif", true); // NOI18N
                    }
                }
            }
        } catch (Exception e) {
            // Problem ==>> use default icon
            Logger.getLogger(InstanceNode.class.getName()).log(Level.WARNING, null, e);
            Logger.getLogger(InstanceNode.class.getName()).log(Level.INFO, "ic = {0}", ic); //NOI18N
            brokenIcon = true;
        } catch (LinkageError e) {
            // #30650 - catch also LinkageError.
            // Problem ==>> use default icon
            Logger.getLogger(InstanceNode.class.getName()).log(Level.WARNING, null, e);
            Logger.getLogger(InstanceNode.class.getName()).log(Level.INFO, "ic = {0}", ic); //NOI18N
            brokenIcon = true;
        }

        return beanInfoIcon;
    }

    /** try to initialize display name.
    */
    public void run() {
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                return;
            }
            Class<?> clazz = ic.instanceClass();
        
        String className = clazz.getName ();
        if (className.equals ("javax.swing.JSeparator") || // NOI18N
            className.equals ("javax.swing.JToolBar$Separator")) { // NOI18N
                
            setDisplayName (NbBundle.getMessage (InstanceDataObject.class,
                "LBL_separator_instance")); // NOI18N
            return;
        }
        // Also specially handle action instances.
        if (Action.class.isAssignableFrom(clazz)) {
            Action action = (Action)ic.instanceCreate();
            // Set node's display name.
            String name = action != null ? (String)action.getValue(Action.NAME) : null;
            
            // #31227 - some action does not implement its name properly.
            // Throw exception with the name of the class.
            if (name == null) {
                DataObject.LOG.warning(
                    "Please attach following information to the issue " + // NOI18N
                    "<http://www.netbeans.org/issues/show_bug.cgi?id=31227>: " + // NOI18N
                    "action " + className + " does not implement SystemAction.getName() or Action.getValue(NAME) properly. It returns null!"); // NOI18N
                setDisplayName(className);
                return;
            }
            
            int amper = name.indexOf ('&');
            if (amper != -1) {
                name = name.substring (0, amper) + name.substring (amper + 1);
            }
            if (name.endsWith ("...")) {// NOI18N
                name = name.substring (0, name.length () - 3);
            }
            name = name.trim ();
            setDisplayName (name);
            return;
        }
        } catch (Exception e) {
            Logger.getLogger(InstanceNode.class.getName()).log(Level.WARNING, null, e);
            setDisplayName(getDataObject().getName());
            return;
        }
    }
    
    /** Try to get display name of the bean.
     */
    private String getNameForBean() {
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                // it must be unrecognized setting
                return NbBundle.getMessage(InstanceNode.class,
                    "LBL_BrokenSettings"); //NOI18N
            }
            Class<?> clazz = ic.instanceClass();
            Method nameGetter;
            try {
                nameGetter = clazz.getMethod ("getName", (Class<?>[]) null); // NOI18N
                if (nameGetter.getReturnType () != String.class) {
                    throw new NoSuchMethodException();
                }
            } catch (NoSuchMethodException e) {
                try {
                    nameGetter = clazz.getMethod ("getDisplayName", (Class<?>[]) null); // NOI18N
                    if (nameGetter.getReturnType () != String.class) {
                        throw new NoSuchMethodException();
                    }
                } catch (NoSuchMethodException ee) {
                    return null;
                }
            }
            Object bean = ic.instanceCreate();
            return (String) nameGetter.invoke (bean);
        } catch (Exception ex) {
            return null;
        }
    }
    
    /** try to find setter setName/setDisplayName, if none declared return null */
    private Method getDeclaredSetter() {
        Method nameSetter = null;
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                return null;
            }
            Class<?> clazz = ic.instanceClass();
            // find the setter for the name
            try {
                nameSetter = clazz.getMethod ("setName", String.class); // NOI18N
            } catch (NoSuchMethodException e) {
                nameSetter = clazz.getMethod ("setDisplayName", String.class); // NOI18N
            }
        } catch (Exception ex) {
        }
        return nameSetter;
    }
    
    @Override
    public void setName(String name) {
        if (!getDataObject().getPrimaryFile().hasExt(XML_EXT)) {
            super.setName(name);
            return ;
        }
        String old = getNameImpl();
        if (old != null && old.equals(name)) {
            return;
        }
        InstanceCookie ic = ic();
        if (ic == null) {
            super.setName(name);
            return;
        }
        
        Method nameSetter = getDeclaredSetter();
        if (nameSetter != null) {
            try {
                Object bean = ic.instanceCreate();
                nameSetter.invoke(bean, new Object[] {name});
                i().scheduleSave();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        super.setName(name);
    }
    
    /** Get the display name for the node.
     * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
     * @return the desired name
    */
    @Override
    public String getDisplayName () {
        String name = (String) getDataObject().getPrimaryFile().
            getAttribute(InstanceDataObject.EA_NAME);
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
                        name = super.getDisplayName();
                    }
                }
            } catch (FileStateInvalidException e) {
                // no fs, do nothing
            }
        }
        return name;
    }
    
    /** try to get name by colling getter on the instance. */
    private String getNameImpl() {
        String name;
        name = getNameForBean();
        if (name == null) {
            name = getName();
        }

        return name;
    }
    
    @Override
    protected Sheet createSheet () {
        Sheet orig;
    
        if (getDataObject ().getPrimaryFile ().hasExt ("ser") || // NOI18N
            getDataObject ().getPrimaryFile ().hasExt (XML_EXT)) {
            orig = new Sheet();
            changeSheet (orig);
        } else {
            // just instance file, change here
            orig = super.createSheet ();
            Sheet.Set props = orig.get (Sheet.PROPERTIES);
            final InstanceCookie ic = ic();
            if (ic == null) {
                props.put (new PropertySupport.ReadOnly<String> (
                    "className", String.class, // NOI18N
                    NbBundle.getMessage (InstanceDataObject.class, "PROP_instance_class"), // NOI18N
                    NbBundle.getMessage (InstanceDataObject.class, "HINT_instance_class") // NOI18N
                ) {
                    public String getValue () {
                        return ic.instanceName ();
                    }
                });
            }
        }
        
        isSheetCreated = true;
        return orig;
    }
        
        
    private void changeSheet (Sheet orig) {
        Sheet.Set props = orig.get (Sheet.PROPERTIES);

        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                return;
            }
            // properties
            BeanInfo beanInfo = Utilities.getBeanInfo (ic.instanceClass ());
            BeanNode.Descriptor descr = BeanNode.computeProperties (ic.instanceCreate (), beanInfo);
            initPList();

            props = Sheet.createPropertiesSet();
            if (descr.property != null) {
                convertProps (props, descr.property, i ());
            }
            orig.put (props);

            if (descr.expert != null && descr.expert.length != 0) {
                Sheet.Set p = Sheet.createExpertSet();
                convertProps (p, descr.expert, i ());
                orig.put (p);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(InstanceNode.class.getName()).log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InstanceNode.class.getName()).log(Level.WARNING, null, ex);
        } catch (IntrospectionException ex) {
            Logger.getLogger(InstanceNode.class.getName()).log(Level.WARNING, null, ex);
        } catch (LinkageError ex) {
            // #30650 - catch also LinkageError.
            Logger.getLogger(InstanceNode.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    
    /** Method that converts properties of an object.
     * @param set set to add properties to
     * @param arr array of Node.Property and Node.IndexedProperty
     * @param ido IDO providing task to invoke when a property changes
     */
    private static final void convertProps (
        Sheet.Set set, Node.Property[] arr, InstanceDataObject ido
    ) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Node.IndexedProperty) {
                set.put (new I ((Node.IndexedProperty)arr[i], ido));
            } else {
                set.put (new P (arr[i], ido));
            }
        }
    }        
    
    /** The method creates a BufferedImage which represents the same Image as the
     * parameter but consumes less memory.
     */
    private static final java.awt.Image toBufferedImage(Image img, boolean load) {
        // load the image
        if (load) {
            new javax.swing.ImageIcon(img);
        }
        
        java.awt.image.BufferedImage rep = createBufferedImage();
        java.awt.Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();
        return rep;
    }

    /** Creates BufferedImage 16x16 and Transparency.BITMASK */
    private static final java.awt.image.BufferedImage createBufferedImage() {
        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().
                                          getDefaultScreenDevice().getDefaultConfiguration().getColorModel(java.awt.Transparency.BITMASK);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage(model,
                model.createCompatibleWritableRaster(16, 16), model.isAlphaPremultiplied(), null);
        return buffImage;
    }
    
    /** Indicate whether the node may be renamed.
     * @return tests {@link DataObject#isRenameAllowed}
     */
    @Override
    public boolean canRename() {
        if (!getDataObject().getPrimaryFile().hasExt(XML_EXT)) {
            return super.canRename();
        }
        return getDeclaredSetter() != null;
    }
    
    /** Indicate whether the node may be destroyed.
     * @return tests {@link DataObject#isDeleteAllowed}
     */
    @Override
    public boolean canDestroy() {
        if (!getDataObject().getPrimaryFile().hasExt(XML_EXT)) {
            return super.canDestroy();
        }
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                return true;
            }
            Class<?> clazz = ic.instanceClass();
            return (!SharedClassObject.class.isAssignableFrom(clazz));
        } catch (Exception ex) {
            return false;
        }
    }
    
    @Override
    public boolean canCut() {
        if (!getDataObject().getPrimaryFile().hasExt(XML_EXT)) {
            return super.canCut();
        }
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                return false;
            }
            Class<?> clazz = ic.instanceClass();
            return (!SharedClassObject.class.isAssignableFrom(clazz));
        } catch (Exception ex) {
            return false;
        }
    }
    
    @Override
    public boolean canCopy() {
        if (!getDataObject().getPrimaryFile().hasExt(XML_EXT)) {
            return super.canCopy();
        }
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                return false;
            }
            Class<?> clazz = ic.instanceClass();
//XXX            if (XMLSettingsSupport.BrokenSettings.class.isAssignableFrom(clazz))
//                return false;
            return (!SharedClassObject.class.isAssignableFrom(clazz));
        } catch (Exception ex) {
            return false;
        }
    }
    
    /** Gets the short description of this feature. */
    @Override
    public String getShortDescription() {
        if (noBeanInfo) {
            return super.getShortDescription();
        }
        
        try {
            InstanceCookie ic = ic();
            if (ic == null) {
                // it must be unrecognized instance
                return getDataObject().getPrimaryFile().toString();
            }
            
            Class<?> clazz = ic.instanceClass();
            java.beans.BeanDescriptor bd = Utilities.getBeanInfo(clazz).getBeanDescriptor();
            String desc = bd.getShortDescription();
            return (desc.equals(bd.getName()))? getDisplayName(): desc;
        } catch (Exception ex) {
            return super.getShortDescription();
        } catch (LinkageError ex) {
            // #30650 - catch also LinkageError.
            return super.getShortDescription();
        }
    }
    
    /* do not want CustomizeBean to be invoked on double-click */
    @Override
    public Action getPreferredAction() {
        return null;
    }
    
    //
    // inner classes - properties
    //
    
    /** A property that delegates every call to original property
     * but when modified, also starts a saving task.
     */
    private static final class P<T> extends Node.Property<T> {
        /** delegate */
        private Node.Property<T> del;
        /** task to executed */
        private InstanceDataObject t;

        public P (Node.Property<T> del, InstanceDataObject t) {
            super (del.getValueType ());
            this.del = del;
            this.t = t;
        }

        @Override
        public void setName(java.lang.String str) {
            del.setName(str);
        }

        @Override
        public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
            del.restoreDefaultValue();
        }

        @Override
        public void setValue(java.lang.String str, java.lang.Object obj) {
            del.setValue(str, obj);
        }

        @Override
        public boolean supportsDefaultValue() {
            return del.supportsDefaultValue();
        }

        public boolean canRead() {
            return del.canRead ();
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return del.getPropertyEditor();
        }

        @Override
        public boolean isHidden() {
            return del.isHidden();
        }

        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return del.getValue ();
        }

        @Override
        public void setExpert(boolean param) {
            del.setExpert(param);
        }

        /** Delegates the set value and also saves the bean.
         */
        public void setValue(T val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            del.setValue (val);
            t.scheduleSave();
        }

        @Override
        public void setShortDescription(java.lang.String str) {
            del.setShortDescription(str);
        }

        @Override
        public boolean isExpert() {
            return del.isExpert();
        }

        public boolean canWrite() {
            return del.canWrite ();
        }

        @Override
        public Class<T> getValueType() {
            return del.getValueType();
        }

        @Override
        public java.lang.String getDisplayName() {
            return del.getDisplayName();
        }

        @Override
        public java.util.Enumeration<String> attributeNames() {
            return del.attributeNames();
        }

        @Override
        public java.lang.String getShortDescription() {
            return del.getShortDescription();
        }

        @Override
        public java.lang.String getName() {
            return del.getName();
        }

        @Override
        public void setHidden(boolean param) {
            del.setHidden(param);
        }

        @Override
        public void setDisplayName(java.lang.String str) {
            del.setDisplayName(str);
        }

        @Override
        public boolean isPreferred() {
            return del.isPreferred();
        }

        @Override
        public java.lang.Object getValue(java.lang.String str) {
            return del.getValue(str);
        }

        @Override
        public void setPreferred(boolean param) {
            del.setPreferred(param);
        }

    } // end of P

    /** A property that delegates every call to original property
     * but when modified, also starts a saving task.
     */
    private static final class I<T> extends Node.IndexedProperty<T, InstanceDataObject> {
        /** delegate */
        private Node.IndexedProperty<T, InstanceDataObject> del;
        /** task to executed */
        private InstanceDataObject t;

        public I (Node.IndexedProperty<T, InstanceDataObject> del, InstanceDataObject t) {
            super (del.getValueType (), del.getElementType ());
            this.del = del;
            this.t = t;
        }

        @Override
        public void setName(java.lang.String str) {
            del.setName(str);
        }

        @Override
        public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
            del.restoreDefaultValue();
        }

        @Override
        public void setValue(java.lang.String str, java.lang.Object obj) {
            del.setValue(str, obj);
        }

        @Override
        public boolean supportsDefaultValue() {
            return del.supportsDefaultValue();
        }

        public boolean canRead() {
            return del.canRead ();
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return del.getPropertyEditor();
        }

        @Override
        public boolean isHidden() {
            return del.isHidden();
        }

        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return del.getValue ();
        }

        @Override
        public void setExpert(boolean param) {
            del.setExpert(param);
        }

        /** Delegates the set value and also saves the bean.
         */
        public void setValue(T val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            del.setValue (val);
            t.scheduleSave();
        }

        @Override
        public void setShortDescription(java.lang.String str) {
            del.setShortDescription(str);
        }

        @Override
        public boolean isExpert() {
            return del.isExpert();
        }

        public boolean canWrite() {
            return del.canWrite ();
        }

        @Override
        public Class<T> getValueType() {
            return del.getValueType();
        }

        @Override
        public java.lang.String getDisplayName() {
            return del.getDisplayName();
        }

        @Override
        public java.util.Enumeration<String> attributeNames() {
            return del.attributeNames();
        }

        @Override
        public java.lang.String getShortDescription() {
            return del.getShortDescription();
        }

        @Override
        public java.lang.String getName() {
            return del.getName();
        }

        @Override
        public void setHidden(boolean param) {
            del.setHidden(param);
        }

        @Override
        public void setDisplayName(java.lang.String str) {
            del.setDisplayName(str);
        }

        @Override
        public boolean isPreferred() {
            return del.isPreferred();
        }

        @Override
        public java.lang.Object getValue(java.lang.String str) {
            return del.getValue(str);
        }

        @Override
        public void setPreferred(boolean param) {
            del.setPreferred(param);
        }

        public boolean canIndexedRead () {
            return del.canIndexedRead ();
        }

        @Override
        public Class<InstanceDataObject> getElementType () {
            return del.getElementType ();
        }

        public InstanceDataObject getIndexedValue (int index) throws
        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return del.getIndexedValue (index);
        }

        public boolean canIndexedWrite () {
            return del.canIndexedWrite ();
        }

        public void setIndexedValue (int indx, InstanceDataObject val) throws
        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            del.setIndexedValue (indx, val);
            t.scheduleSave();
        }

        @Override
        public PropertyEditor getIndexedPropertyEditor () {
            return del.getIndexedPropertyEditor ();
        }
    } // end of I
    
    /** Derived from BeanChildren and allow replace beancontext. */
    private static final class InstanceChildren extends Children.Keys implements PropertyChangeListener {
        java.lang.ref.WeakReference<PropertyChangeListener> dobjListener;
        InstanceDataObject dobj;
        Object bean;
        ContextL contextL = null;
        
        public InstanceChildren(InstanceDataObject dobj) {
            this.dobj = dobj;
        }
        
        @Override
        protected void addNotify () {
            super.addNotify();
            
            PropertyChangeListener p = org.openide.util.WeakListeners.propertyChange(this, dobj);
            dobjListener = new java.lang.ref.WeakReference<PropertyChangeListener>(p);
            dobj.addPropertyChangeListener(p);
            // attaches a listener to the bean
            contextL = new ContextL (this);
            propertyChange(null);
        }
        
        @Override
        protected void removeNotify () {
            if (contextL != null && bean != null) {
                ((BeanContext) bean).removeBeanContextMembershipListener(contextL);
            }
            contextL = null;
            
            PropertyChangeListener p = dobjListener.get();
            if (p != null) {
                dobj.removePropertyChangeListener(p);
                dobjListener.clear();
            }

            setKeys (java.util.Collections.emptySet());
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt != null && !evt.getPropertyName().equals(InstanceDataObject.PROP_COOKIE)) return;
            
            if (contextL != null && bean != null) {
                ((BeanContext) bean).removeBeanContextMembershipListener(contextL);
            }
            
            try {
                InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
                if (ic == null) {
                    bean = null;
                    return;
                }
                Class<?> clazz = ic.instanceClass();
                if (BeanContext.class.isAssignableFrom(clazz)) {
                    bean = ic.instanceCreate();
                } else if (BeanContextProxy.class.isAssignableFrom(clazz)) {
                    bean = ((BeanContextProxy) dobj.instanceCreate()).getBeanContextProxy();
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
                setKeys(java.util.Collections.emptySet());
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
            if (bean == null) {
                return new Node[0];
            }
            
            try {
                if (key instanceof java.beans.beancontext.BeanContextSupport) {
                    java.beans.beancontext.BeanContextSupport bcs = (java.beans.beancontext.BeanContextSupport)key;

                    if (((BeanContext) ctx).contains (bcs.getBeanContextPeer())) {
                        // sometimes a BeanContextSupport occures in the list of
                        // beans children even there is its peer. we think that
                        // it is desirable to hide the context if the peer is
                        // also present
                        return new Node[0];
                    }
                }

                return new Node[] { new BeanContextNode<Object> (key, dobj) };
            } catch (IntrospectionException ex) {
                // ignore the exception
                return new Node[0];
            }
        }
        
        /** Context listener.
        */
        private static final class ContextL implements BeanContextMembershipListener {
            /** weak reference to the BeanChildren object */
            private java.lang.ref.WeakReference<InstanceChildren> ref;

            /** Constructor */
            ContextL (InstanceChildren bc) {
                ref = new java.lang.ref.WeakReference<InstanceChildren> (bc);
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
        InstanceDataObject task;
        public BeanFactoryImpl(InstanceDataObject task) {
            this.task = task;
        }
        
        /** @return bean node */
        public Node createNode (Object bean) throws IntrospectionException {
            return new BeanContextNode<Object> (bean, task);
        }
    }
    
    private static class BeanContextNode<T> extends BeanNode<T> {
        public BeanContextNode(T bean, InstanceDataObject task) throws IntrospectionException {
            super(bean, getChildren(bean, task));
            changeSheet(getSheet(), task);
        }
        
        private void changeSheet(Sheet orig, InstanceDataObject task) {
            Sheet.Set props = orig.get (Sheet.PROPERTIES);
            if (props != null) {
                convertProps (props, props.getProperties(), task);
            }

            props = orig.get(Sheet.EXPERT);
            if (props != null) {
                convertProps (props, props.getProperties(), task);
            }
        }
        private static Children getChildren (Object bean, InstanceDataObject task) {
            if (bean instanceof BeanContext) {
                return new BeanChildren((BeanContext) bean, new BeanFactoryImpl(task));
            }
            if (bean instanceof BeanContextProxy) {
                java.beans.beancontext.BeanContextChild bch = ((BeanContextProxy)bean).getBeanContextProxy();
                if (bch instanceof BeanContext) {
                    return new BeanChildren((BeanContext) bch, new BeanFactoryImpl(task));
                }
            }
            return Children.LEAF;
        }
        
        // #7925
        @Override
        public boolean canDestroy() {
            return false;
        }
        
    }
    
    /** Property change listener to update the properties of the node and
    * also the name of the node (sometimes)
    */
    private final class PropL extends Object implements PropertyChangeListener {
        private boolean doNotListen = false;
        PropL() {}
        
        public void propertyChange(PropertyChangeEvent e) {
            if (doNotListen) {
                return;
            }
            firePropertyChange (e.getPropertyName (), e.getOldValue (), e.getNewValue ());
        }
        
        public void destroy() {
            doNotListen = true;
        }
    }
        
}
