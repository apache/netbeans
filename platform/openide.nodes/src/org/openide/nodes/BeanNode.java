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

package org.openide.nodes;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.Customizer;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextProxy;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
* <p>
* Represents a JavaBeans component as a node.
* </p>
*
* <p>
* You may use this node type for an already-existing JavaBean (possibly
* using BeanContext) in order for its JavaBean properties to be reflected
* as corresponding node properties. Thus, it serves as a compatibility wrapper.
* </p>
*
* <p>
* The bean passed in the constructor will be available in the node's lookup,
* though not directly.  Instead, the node's <code>Lookup</code> will contain
* an <code>InstanceCookie</code> from which you can retrieve the bean instance.
* </p>
*
* @author Jan Jancura, Ian Formanek, Jaroslav Tulach
* @param <T> the type of bean to be represented
*/
public class BeanNode<T> extends AbstractNode {
    // static ..................................................................................................................

    /** Icon base for bean nodes */
    private static final String ICON_BASE = "org/openide/nodes/beans.gif"; // NOI18N

    // variables .............................................................................................................

    /** bean */
    private final T bean;

    /** bean info for the bean */
    private BeanInfo beanInfo;

    /** functions to operate on beans */
    private Method nameGetter = null;
    private Method nameSetter = null;

    /** remove PropertyChangeListener method */
    private Method removePCLMethod = null;

    /** listener for properties */
    private PropL propertyChangeListener = null;

    /** is synchronization of name in progress */
    private boolean synchronizeName;
    
    // init ..................................................................................................................

    /**
    * Constructs a node for a JavaBean. If the bean is a {@link BeanContext},
    * creates a child list as well.
    *
    * @param bean the bean this node will be based on
    * @throws IntrospectionException if the bean cannot be analyzed
    */
    public BeanNode(T bean) throws IntrospectionException {
        this(bean, null, null);
    }

    /** Constructs a node for a JavaBean with a defined child list.
    * Intended for use by subclasses with different strategies for computing the children.
    * @param bean the bean this node will be based on
    * @param children children for the node (default if null)
    * @throws IntrospectionException if the bean cannot be analyzed
    */
    protected BeanNode(T bean, Children children)
    throws IntrospectionException {
        this(bean, children, null);
    }

    /** Constructs a node for a JavaBean.
     * The subclass can provide its own {@link Children} implementation
     * or leave the default implementation.
     * It can also provide a Lookup, but if you provide one, please do not call
     * methods {@link #getCookieSet} and {@link #setCookieSet} they will
     * throw an exception.
     * <p>More info on the correct usage of constructor with Lookup can be found
     * in the {@link Node#Node(org.openide.nodes.Children, org.openide.util.Lookup)}
     * javadoc.
     *
     * @param bean the bean this node will be based on
     * @param children children for the node (default if null)
     * @param lkp the lookup to provide content of {@link #getLookup}
     *   and also {@link #getCookie}
     * @throws IntrospectionException if the bean cannot be analyzed
     * @since 6.9
     */
    protected BeanNode(T bean, Children children, Lookup lkp)
    throws IntrospectionException {
        super((children == null) ? getChildren(bean) : children, lkp);

        if (bean == null) {
            throw new NullPointerException("cannot make a node for a null bean"); // NOI18N
        }

        this.bean = bean;

        try {
            initialization(lkp != null);
        } catch (IntrospectionException ie) {
            throw ie;
        } catch (RuntimeException re) {
            throw mkie(re);
        } catch (LinkageError le) {
            throw mkie(le);
        }
    }
    
    private static Children getChildren(Object bean) {
        if (bean instanceof BeanContext) {
            return new BeanChildren((BeanContext) bean);
        }

        if (bean instanceof BeanContextProxy) {
            BeanContextChild bch = ((BeanContextProxy) bean).getBeanContextProxy();

            if (bch instanceof BeanContext) {
                return new BeanChildren((BeanContext) bch);
            }
        }

        return Children.LEAF;
    }

    private static IntrospectionException mkie(Throwable t) {
        return (IntrospectionException) new IntrospectionException(t.toString()).initCause(t);
    }

    /** Set whether or not to keep the node name and Bean name synchronized automatically.
    * If enabled, the node will listen to changes in the name of the bean
    * and update the (system) name of the node appropriately. The name of the bean can
    * be obtained by calling <code>getName ()</code>, <code>getDisplayName ()</code> or from {@link BeanDescriptor#getDisplayName}.
    * <p>Also when the (system) name of the node is changing, the change propagates if possible to
    * methods <code>setName (String)</code> or <code>setDisplayName (String)</code>. (This
    * does not apply to setting the display name of the node, however.)
    * <P>
    * By default this feature is turned on.
    *
    * @param watch <code>true</code> if the name of the node should be synchronized with
    *   the name of the bean, <code>false</code> if the name of the node should be independent
    *   or manually updated
    *
    */
    protected void setSynchronizeName(boolean watch) {
        synchronizeName = watch;
    }

    /** Provides access to the bean represented by this BeanNode.
    * @return instance of the bean represented by this BeanNode
    */
    protected T getBean() {
        return bean;
    }

    /** Detaches all listeners from the bean and destroys it.
    * @throws IOException if there was a problem
    */
    @Override
    public void destroy() throws IOException {
        if (removePCLMethod != null) {
            try {
                Object o = Beans.getInstanceOf(bean, removePCLMethod.getDeclaringClass());
                removePCLMethod.invoke(o, new Object[] { propertyChangeListener });
            } catch (Exception e) {
                NodeOp.exception(e);
            }
        }

        super.destroy();
    }

    /** Can this node be removed?
    * @return <CODE>true</CODE> in this implementation
    */
    @Override
    public boolean canDestroy() {
        return true;
    }

    /** Set the node name.
    * Also may attempt to change the name of the bean,
    * according to {@link #setSynchronizeName}.
    * @param s the new name
    */
    @Override
    public void setName(String s) {
        if (synchronizeName) {
            Method m = nameSetter;

            if (m != null) {
                try {
                    m.invoke(bean, new Object[] { s });
                } catch (Exception e) {
                    NodeOp.exception(e);
                }
            }
        }

        super.setName(s);
    }

    /** Can this node be renamed?
    * @return <code>true</code> if there is no name synchronization, or there is
    *         a valid setter method for the name
    */
    @Override
    public boolean canRename() {
        return !synchronizeName || (nameSetter != null);
    }

    /** Get an icon for this node in the closed state.
    * Uses the Bean's icon if possible.
    *
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use
    */
    @Override
    public Image getIcon(int type) {
        Image image = beanInfo.getIcon(type);

        if (image != null) {
            return image;
        }

        return super.getIcon(type);
    }

    /** Get an icon for this node in the open state.
    *
    * @param type type constants
    * @return icon to use. The default implementation just uses {@link #getIcon}.
    */
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public HelpCtx getHelpCtx() {
        HelpCtx h = HelpCtx.findHelp(bean);

        if (h != HelpCtx.DEFAULT_HELP) {
            return h;
        } else {
            return new HelpCtx(BeanNode.class);
        }
    }

    /** Prepare node properties based on the bean, storing them into the current property sheet.
    * Called when the bean info is ready.
    * This implementation always creates a set for standard properties
    * and may create a set for expert ones if there are any.
    * @see #computeProperties
    * @param bean bean to compute properties for
    * @param info information about the bean
    */
    protected void createProperties(T bean, BeanInfo info) {
        Descriptor d = computeProperties(bean, info);

        Sheet sets = getSheet();
        Sheet.Set pset = Sheet.createPropertiesSet();
        pset.put(d.property);

        BeanDescriptor bd = info.getBeanDescriptor();

        if ((bd != null) && (bd.getValue("propertiesHelpID") != null)) { // NOI18N      
            pset.setValue("helpID", bd.getValue("propertiesHelpID")); // NOI18N
        }

        sets.put(pset);

        if (d.expert.length != 0) {
            Sheet.Set eset = Sheet.createExpertSet();
            eset.put(d.expert);

            if ((bd != null) && (bd.getValue("expertHelpID") != null)) { // NOI18N      
                eset.setValue("helpID", bd.getValue("expertHelpID")); // NOI18N
            }

            sets.put(eset);
        }
    }

    /** Can this node be copied?
    * @return <code>true</code> in the default implementation
    */
    @Override
    public boolean canCopy() {
        return true;
    }

    /** Can this node be cut?
    * @return <code>false</code> in the default implementation
    */
    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        return NodeOp.createFromNames(
            new String[] { "Copy", null, "Tools", "Properties" // NOI18N
        }
        );
    }

    /* Test if there is a customizer for this node. If <CODE>true</CODE>
    * the customizer can be obtained via <CODE>getCustomizer</CODE> method.
    *
    * @return <CODE>true</CODE> if there is a customizer.
    */
    @Override
    public boolean hasCustomizer() {
        // true if we have already computed beanInfo and it has customizer class
        return beanInfo.getBeanDescriptor().getCustomizerClass() != null;
    }

    /* Returns the customizer component.
    * @return the component or <CODE>null</CODE> if there is no customizer
    */
    @Override
    public java.awt.Component getCustomizer() {
        Class<?> clazz = beanInfo.getBeanDescriptor().getCustomizerClass();

        if (clazz == null) {
            return null;
        }

        Object o;

        try {
            o = clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            NodeOp.exception(e);
            return null;
        }

        if (!(o instanceof Customizer)) {
            // no customizer => no fun
            // [PENDING] this ought to perform some sort of notification!
            return null;
        }

        Customizer cust = ((Customizer) o);

        TMUtil.attachCustomizer(this, cust);

        // looking for the component
        java.awt.Component comp = null;

        if (o instanceof java.awt.Component) {
            comp = (java.awt.Component) o;
        } else {
            // create the dialog from descriptor
            comp = TMUtil.createDialog(o);
        }

        if (comp == null) {
            // no component provided
            return null;
        }

        cust.setObject(bean);

        if (removePCLMethod == null) {
            cust.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
                    }
                }
            );
        }

        return comp;
    }

    /** Computes a descriptor for properties from a bean info.
    * <p>Property code names are taken from the property descriptor names
     * according to the JavaBeans specification. For example, a pair of
     * methods <code>getFoo</code> and <code>setFoo</code> would result in
     * a node property with code name <code>foo</code>. If you call
     * <code>MyBean.setFoo(...)</code>, this should result in a property
     * change event with name <code>foo</code>; if you are using these
     * properties in some other context (attached to something other than
     * a <code>BeanNode</code>) then be careful to fire changes with the correct
     * name, or there may be problems with refreshing display of the property etc.
    * @param bean bean to create properties for
    * @param info about the bean
    * @return three property lists
    */
    public static Descriptor computeProperties(Object bean, BeanInfo info) {
        List<Node.Property> property = new ArrayList<>();
        List<Node.Property> expert = new ArrayList<>();
        List<Node.Property> hidden = new ArrayList<>();

        PropertyDescriptor[] propertyDescriptor = info.getPropertyDescriptors();

        int k = propertyDescriptor.length;

        for (int i = 0; i < k; i++) {
            if (propertyDescriptor[i].getPropertyType() == null) {
                continue;
            }

            Node.Property prop;

            if (propertyDescriptor[i] instanceof IndexedPropertyDescriptor) {
                IndexedPropertyDescriptor p = (IndexedPropertyDescriptor) propertyDescriptor[i];

                if ((p.getReadMethod() != null) && (!p.getReadMethod().getReturnType().isArray())) {
                    // this is fix for #17728. This situation should never happen
                    // But if the BeanInfo (IndexedPropertyDescriptor) is wrong
                    // we will ignore this property
                    continue;
                }

                IndexedPropertySupport support = new IndexedPropertySupport(
                        bean, p.getPropertyType(), p.getIndexedPropertyType(), p.getReadMethod(), p.getWriteMethod(),
                        p.getIndexedReadMethod(), p.getIndexedWriteMethod()
                    );
                support.setName(p.getName());
                support.setDisplayName(p.getDisplayName());
                support.setShortDescription(p.getShortDescription());

                for (Enumeration e = p.attributeNames(); e.hasMoreElements();) {
                    String aname = (String) e.nextElement();
                    support.setValue(aname, p.getValue(aname));
                }

                prop = support;
            } else {
                PropertyDescriptor p = propertyDescriptor[i];

                // Note that PS.R sets the method accessible even if it is e.g.
                // defined as public in a package-accessible superclass.
                PropertySupport.Reflection support = new PropertySupport.Reflection(
                        bean, p.getPropertyType(), p.getReadMethod(), p.getWriteMethod()
                    );
                support.setName(p.getName());
                support.setDisplayName(p.getDisplayName());
                support.setShortDescription(p.getShortDescription());
                support.setPropertyEditorClass(p.getPropertyEditorClass());

                for (Enumeration e = p.attributeNames(); e.hasMoreElements();) {
                    String aname = (String) e.nextElement();
                    support.setValue(aname, p.getValue(aname));
                }

                prop = support;
            }

            // Propagate helpID's.
            Object help = propertyDescriptor[i].getValue("helpID"); // NOI18N

            if (help instanceof String) {
                prop.setValue("helpID", help); // NOI18N
            }

            // Add to right category.
            if (propertyDescriptor[i].isHidden()) {
                // hidden property
                hidden.add(prop);
            } else {
                if (propertyDescriptor[i].isExpert()) {
                    expert.add(prop);
                    prop.setExpert(true);
                } else {
                    property.add(prop);
                }
            }
        }
         // for

        return new Descriptor(property, expert, hidden);
    }

    //
    //
    // Initialization methods
    //
    //

    /** Performs initialization of the node
    */
    private void initialization(boolean hasLookup) throws IntrospectionException {
        setIconBaseWithExtension(ICON_BASE);

        setSynchronizeName(true);

        // Find the first public superclass of the actual class.
        // Should not introspect on a private class, because then the method objects
        // used for the property descriptors will not be callable without an
        // IllegalAccessException, even if overriding a public method from a public superclass.
        Class<?> clazz = bean.getClass();

        while (!Modifier.isPublic(clazz.getModifiers()) && !hasExplicitBeanInfo(clazz)) {
            clazz = clazz.getSuperclass();

            if (clazz == null) {
                clazz = Object.class; // in case it was an interface
            }
        }

        beanInfo = Utilities.getBeanInfo(clazz);

        // resolving the name of this bean
        registerName();
        setNameSilently(getNameForBean());

        BeanDescriptor descriptor = beanInfo.getBeanDescriptor();
        String sd = descriptor.getShortDescription();

        if (!Utilities.compareObjects(sd, descriptor.getDisplayName())) {
            setShortDescription(sd);
        }

        // add propertyChangeListener
        EventSetDescriptor[] eventSetDescriptors = beanInfo.getEventSetDescriptors();
        int i;
        int k = eventSetDescriptors.length;
        Method method = null;

        for (i = 0; i < k; i++) {
            method = eventSetDescriptors[i].getAddListenerMethod();

            if (
                (method != null) && method.getName().equals("addPropertyChangeListener") && // NOI18N
                    Modifier.isPublic(method.getModifiers())
            ) {
                break;
            }
        }

        if (i != k) {
            try {
                Object o = Beans.getInstanceOf(bean, method.getDeclaringClass());
                propertyChangeListener = new PropL();
                method.invoke(o, new Object[] { WeakListeners.propertyChange(propertyChangeListener, o) });
                removePCLMethod = eventSetDescriptors[i].getRemoveListenerMethod();
            } catch (Exception e) {
                // Warning, not info: likely to call e.g. getters or other things used
                // during startup of the bean, so it is not good to swallow errors here
                // (e.g. SharedClassObject.initialize throws RuntimeException -> it is
                // caught here and probably someone wants to know).
                Exceptions.attachMessage(e,
                                         "Trying to invoke " + method +
                                         " where introspected class is " +
                                         clazz.getName()); // NOI18N
                NodeOp.warning(e);
            }
        }

        createProperties(bean, beanInfo);

        for (Enumeration e = beanInfo.getBeanDescriptor().attributeNames(); e.hasMoreElements();) {
            String aname = (String) e.nextElement();
            setValue(aname, beanInfo.getBeanDescriptor().getValue(aname));
        }

        if (!hasLookup) {
            Node.Cookie instanceCookie = TMUtil.createInstanceCookie(bean);

            if (instanceCookie != null) {
                getCookieSet().add(instanceCookie);
            }
        }
    }

    /** Checks whether there is an explicit bean info for given class.
    * @param clazz the class to test
    * @return true if explicit bean info exists
    */
    private boolean hasExplicitBeanInfo(Class<?> clazz) {
        String className = clazz.getName();
        int indx = className.lastIndexOf('.');
        className = className.substring(indx + 1);

        String[] paths = Introspector.getBeanInfoSearchPath();

        for (int i = 0; i < paths.length; i++) {
            String s = paths[i] + '.' + className + "BeanInfo"; // NOI18N

            try {
                // test if such class exists
                Class.forName(s);

                return true;
            } catch (ClassNotFoundException ex) {
                // OK, this is normal.
            }
        }

        return false;
    }

    // name resolving methods

    /**
    * Finds setter and getter methods for the name of the bean. Resisters listener
    * for changing of name.
    */
    private void registerName() {
        // [PENDING] ought to use introspection, rather than look up the methods by name  --jglick
        Class<?> clazz = bean.getClass();

        // Do not want to use getName, even if public, on a private class:
        while (!Modifier.isPublic(clazz.getModifiers())) {
            clazz = clazz.getSuperclass();

            if (clazz == null) {
                clazz = Object.class;
            }
        }

        // find getter for the name
        try {
            try {
                nameGetter = clazz.getMethod("getName"); // NOI18N

                if (nameGetter.getReturnType() != String.class) {
                    throw new NoSuchMethodException();
                }
            } catch (NoSuchMethodException e) {
                try {
                    nameGetter = clazz.getMethod("getDisplayName"); // NOI18N

                    if (nameGetter.getReturnType() != String.class) {
                        throw new NoSuchMethodException();
                    }
                } catch (NoSuchMethodException ee) {
                    nameGetter = null;

                    return;
                }
            }
        } catch (SecurityException se) {
            NodeOp.exception(se);
            nameGetter = null;

            return;
        }

        // this code tests wheter everything is fine and the getter is
        // invokable
        try {
            // make sure this is cast to String too:
            String result = (String) nameGetter.invoke(bean);
        } catch (Exception e) {
            Exceptions.attachMessage(e,
                                     "Bad method: " + clazz.getName() + "." +
                                     nameGetter.getName());
            Logger.getLogger(BeanNode.class.getName()).log(Level.WARNING, null, e);

            nameGetter = null;

            return;
        }

        // find the setter for the name
        try {
            try {
                // tries to find method setName (String)
                nameSetter = clazz.getMethod("setName", String.class); // NOI18N

                if (nameSetter.getReturnType() != Void.TYPE) {
                    throw new NoSuchMethodException();
                }
            } catch (NoSuchMethodException e) {
                try {
                    nameSetter = clazz.getMethod("setDisplayName", String.class); // NOI18N

                    if (nameSetter.getReturnType() != Void.TYPE) {
                        throw new NoSuchMethodException();
                    }
                } catch (NoSuchMethodException ee) {
                    nameSetter = null;
                }
            }
        } catch (SecurityException se) {
            NodeOp.exception(se);
        }
    }

    /**
    * Returns name of the bean.
    */
    private String getNameForBean() {
        if (nameGetter != null) {
            try {
                String name = (String) nameGetter.invoke(bean);

                return (name != null) ? name : ""; // NOI18N
            } catch (Exception ex) {
                NodeOp.warning(ex);
            }
        }

        BeanDescriptor descriptor = beanInfo.getBeanDescriptor();

        return descriptor.getDisplayName();
    }

    /** To allow inner classes to access the super.setName method.
    */
    void setNameSilently(String name) {
        super.setName(name);
    }

    @Override
    public Action getPreferredAction() {
        // default action is org.openide.actions.PropertiesAction
        SystemAction[] arr = NodeOp.createFromNames(new String[] { "Properties" }); // NOI18N

        return (arr.length == 1) ? arr[0] : null;
    }

    /** Descriptor of three types of properties. Regular,
    * expert and hidden.
    */
    public static final class Descriptor extends Object {
        /** Regular properties. */
        public final Node.Property[] property;

        /** Expert properties. */
        public final Node.Property[] expert;

        /** Hidden properties. */
        public final Node.Property[] hidden;

        /** private constructor */
        Descriptor(List<Node.Property> p, List<Node.Property> e, List<Node.Property> h) {
            property = new Node.Property[p.size()];
            p.toArray(property);

            expert = new Node.Property[e.size()];
            e.toArray(expert);

            hidden = new Node.Property[h.size()];
            h.toArray(hidden);
        }
    }

    /** Property change listener to update the properties of the node and
    * also the name of the node (sometimes)
    */
    private final class PropL extends Object implements PropertyChangeListener {
        PropL() {
        }

        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();

            if (name == null) {
                firePropertyChange(null, e.getOldValue(), e.getNewValue());
            } else {
                PropertyDescriptor[] arr = beanInfo.getPropertyDescriptors();

                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].isHidden()) {
                        continue;
                    }

                    if (name.equals(arr[i].getName())) {
                        firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());

                        break;
                    }
                }
            }

            if (synchronizeName) {
                if ((name == null) || name.equals("name") || name.equals("displayName")) { // NOI18N

                    String newName = getNameForBean();

                    if (!newName.equals(getName())) {
                        setNameSilently(newName);
                    }
                }
            }
        }
    }
}
