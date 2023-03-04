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

package org.openide.nodes;

import java.beans.Beans;
import java.beans.FeatureDescriptor;
import java.beans.Introspector;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** Support class for <code>Node.Property</code>.
 *
 * @param <T> the type of the represented property.
 *
 * @author Jan Jancura, Jaroslav Tulach, Ian Formanek
 */
public abstract class PropertySupport<T> extends Node.Property<T> {
    /** flag whether the property is readable */
    private boolean canR;

    /** flag whether the property is writable */
    private boolean canW;

    /** Constructs a new support.
    * @param name        the name of the property
    * @param type        the class type of the property
    * @param displayName the display name of the property
    * @param canR        whether the property is readable
    * @param canW        whether the property is writable
    */
    public PropertySupport(
        String name, Class<T> type, String displayName, String shortDescription, boolean canR, boolean canW
    ) {
        super(type);
        this.setName(name);
        setDisplayName(displayName);
        setShortDescription(shortDescription);
        this.canR = canR;
        this.canW = canW;
    }

    /* Can read the value of the property.
    * Returns the value passed into constructor.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    @Override
    public boolean canRead() {
        return canR;
    }

    /* Can write the value of the property.
    * Returns the value passed into constructor.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    @Override
    public boolean canWrite() {
        return canW;
    }

    /**
     * Like {@link Class#cast} but handles primitive types.
     * See JDK #6456930.
     */
    @SuppressWarnings("unchecked")
    static <T> T cast(Class<T> c, Object o) {
        if (c.isPrimitive()) {
            // Could try to actually type-check it, but never mind.
            return (T) o;
        } else {
            return c.cast(o);
        }
    }

    /**
     * Fluent wrapper method for {@link #setDisplayName(java.lang.String)}.
     *
     * @param displayName the display name to set.
     * @since 7.62
     *
     * @return this instance
     */
    public final PropertySupport<T> withDisplayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    /**
     * Fluent wrapper method for {@link #setShortDescription(java.lang.String)}.
     *
     * @param shortDescription short description
     * @since 7.62
     * @return this instance
     */
    public final PropertySupport<T> withShortDescription(String shortDescription) {
        setShortDescription(shortDescription);
        return this;
    }

    /**
     * Creates a "virtual" property where getter and setter are backed by the
     * provided {@link Supplier} and {@link Consumer} functional interfaces.
     * @param <T> the type of the property
     * @param name the name of the property
     * @param valueType the type of the property
     * @param supplier the getter functional interface, can be {@code null} for write-only properties.
     * @param consumer the setter functional interface, can be {@code null} for read-only properties.
     *
     * @since 7.62
     * @return a {@link PropertySupport} instance where getter and setter are
     *         backed by the provided functional interfaces.
     */
    public static <T> PropertySupport<T> readWrite(String name, Class<T> valueType, Supplier<T> supplier, Consumer<T> consumer) {
        return new FunctionalProperty<>(name, valueType, supplier, consumer);
    }

    /**
     * Creates a read-only "virtual" property where getter is backed by the
     * provided {@link Supplier} functional interface.
     * @param <T> the type of the property
     * @param name the name of the property
     * @param valueType the type of the property
     * @param supplier the getter functional interface.
     *
     * @since 7.62
     * @return a read-only {@link PropertySupport} instance where getter is
     *         backed by the provided functional interface.
     */
    public static <T> PropertySupport<T> readOnly(String name, Class<T> valueType, Supplier<T> supplier) {
        return new FunctionalProperty<>(name, valueType, supplier, null);
    }
    /**
     * Creates a write-only "virtual" property where setter is backed by the
     * provided {@link Consumer} functional interface.
     * @param <T> the type of the property
     * @param name the name of the property
     * @param valueType the type of the property
     * @param consumer the setter functional interface.
     *
     * @since 7.62
     * @return a write-only {@link PropertySupport} instance where setter is
     *         backed by the provided functional interface.
     */
    public static <T> PropertySupport<T> writeOnly(String name, Class<T> valueType, Consumer<T> consumer) {
        return new FunctionalProperty<>(name, valueType, null, consumer);
    }

    /**
     * Support for properties from Java Reflection.
     * Since 7.19, the {@link FeatureDescriptor#getName} will be set automatically.
     */
    public static class Reflection<T> extends Node.Property<T> {
        /** Instance of a bean. */
        protected Object instance;

        /** setter method */
        private Method setter;

        /** getter method */
        private Method getter;

        /** class of property editor */
        private Class<? extends PropertyEditor> propertyEditorClass;

        /** Create a support with method objects specified.
        * The methods must be public.
        * @param instance (Bean) object to work on
        * @param valueType type of the property
        * @param getter getter method, can be <code>null</code>
        * @param setter setter method, can be <code>null</code>
        * @throws IllegalArgumentException if the methods are not public
        */
        public Reflection(Object instance, Class<T> valueType, Method getter, Method setter) {
            super(valueType);

            if ((getter != null) && !Modifier.isPublic(getter.getModifiers())) {
                throw new IllegalArgumentException("Cannot use a non-public getter " + getter); // NOI18N
            }

            if ((setter != null) && !Modifier.isPublic(setter.getModifiers())) {
                throw new IllegalArgumentException("Cannot use a non-public setter " + setter); // NOI18N
            }

            if (getter != null) {
                setName(Introspector.decapitalize(getter.getName().replaceFirst("^(get|is|has)", "")));
            } else if (setter != null) {
                setName(Introspector.decapitalize(setter.getName().replaceFirst("^set", "")));
            }

            this.instance = instance;
            this.setter = setter;
            this.getter = getter;
        }

        /** Create a support with methods specified by name.
        * The instance class will be examined for the named methods.
        * But if the instance class is not public, the nearest public superclass
        * will be used instead, so that the getters and setters remain accessible.
        * @param instance (Bean) object to work on
        * @param valueType type of the property
        * @param getter name of getter method, can be <code>null</code>
        * @param setter name of setter method, can be <code>null</code>
        * @exception NoSuchMethodException if the getter or setter methods cannot be found
        */
        public Reflection(Object instance, Class<T> valueType, String getter, String setter)
        throws NoSuchMethodException {
            this(
                instance, valueType,
                (
            // find the getter ()
            getter == null) ? null : findAccessibleClass(instance.getClass()).getMethod(getter),
                (
            // find the setter (valueType)
            setter == null) ? null : findAccessibleClass(instance.getClass()).getMethod(
                    setter, new Class<?>[] { valueType }
                )
            );
        }

        // [PENDING] should use Beans API in case there is overriding BeanInfo  --jglick

        /** Create a support based on the property name.
        * The getter and setter methods are constructed by capitalizing the first
        * letter in the name of propety and prefixing it with <code>get</code> and
        * <code>set</code>, respectively.
        *
        * @param instance object to work on
        * @param valueType type of the property
        * @param property name of property
        * @exception NoSuchMethodException if the getter or setter methods cannot be found
        */
        public Reflection(Object instance, Class<T> valueType, String property)
        throws NoSuchMethodException {
            this(
                instance, valueType, findGetter(instance, valueType, property),
                findAccessibleClass(instance.getClass()).getMethod(
                    firstLetterToUpperCase(property, "set"), valueType
                )
            );
        }

        /** Find the nearest superclass (or same class) that is public to this one. */
        private static <C> Class<? super C> findAccessibleClass(Class<C> clazz) {
            if (Modifier.isPublic(clazz.getModifiers())) {
                return clazz;
            } else {
                Class<? super C> sup = clazz.getSuperclass();

                if (sup == null) {
                    return Object.class; // handle interfaces
                }

                return findAccessibleClass(sup);
            }
        }

        /** Helper method to convert the first letter of a string to uppercase.
        * And prefix the string with some next string.
        */
        private static String firstLetterToUpperCase(String s, String pref) {
            switch (s.length()) {
            case 0:
                return pref;

            case 1:
                return pref + Character.toUpperCase(s.charAt(0));

            default:
                return pref + Character.toUpperCase(s.charAt(0)) + s.substring(1);
            }
        }

        // Finds the proper getter
        private static Method findGetter(Object instance, Class<?> valueType, String property)
        throws NoSuchMethodException {
            NoSuchMethodException nsme;

            try {
                return findAccessibleClass(instance.getClass()).getMethod(
                    firstLetterToUpperCase(property, "get")
                );
            } catch (NoSuchMethodException e) {
                if (valueType != boolean.class) {
                    throw e;
                } else {
                    nsme = e;
                }
            }

            // Is of type boolean and "get" getter does not exist
            try {
                return findAccessibleClass(instance.getClass()).getMethod(
                    firstLetterToUpperCase(property, "is")
                );
            } catch (NoSuchMethodException e) {
                throw e;
            }
        }

        /* Can read the value of the property.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        public boolean canRead() {
            return getter != null;
        }

        /* Getter for the value.
        * @return the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        public T getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (getter == null) {
                throw new IllegalAccessException();
            }

            Object valideInstance = Beans.getInstanceOf(instance, getter.getDeclaringClass());

            try {
                try {
                    return cast(getValueType(), getter.invoke(valideInstance));
                } catch (IllegalAccessException ex) {
                    try {
                        getter.setAccessible(true);

                        return cast(getValueType(), getter.invoke(valideInstance));
                    } finally {
                        getter.setAccessible(false);
                    }
                }
            } catch (IllegalArgumentException iae) {
                //Provide a better message for debugging
                StringBuilder sb = new StringBuilder("Attempted to invoke method ");
                sb.append(getter.getName());
                sb.append(" from class ");
                sb.append(getter.getDeclaringClass().getName());
                sb.append(" on an instance of ");
                sb.append(valideInstance.getClass().getName());
                sb.append(" Problem:");
                sb.append(iae.getMessage());
                throw (IllegalArgumentException) new IllegalArgumentException(sb.toString()).initCause(iae);
            }
        }

        /* Can write the value of the property.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        @Override
        public boolean canWrite() {
            return setter != null;
        }

        /* Setter for the value.
        * @param val the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        @Override
        public void setValue(T val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (setter == null) {
                throw new IllegalAccessException();
            }

            Object valideInstance = Beans.getInstanceOf(instance, setter.getDeclaringClass());

            try {
                setter.invoke(valideInstance, val);
            } catch (IllegalAccessException ex) {
                try {
                    setter.setAccessible(true);
                    setter.invoke(valideInstance, val);
                } finally {
                    setter.setAccessible(false);
                }
            }
        }

        /* Returns property editor for this property.
        * @return the property editor or <CODE>null</CODE> if there should not be
        *    any editor.
        */
        @Override
        public PropertyEditor getPropertyEditor() {
            if (propertyEditorClass != null) {
                try {
                    return propertyEditorClass.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException | SecurityException | IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return super.getPropertyEditor();
        }

        /** Set the property editor explicitly.
        * @param clazz class type of the property editor
        */
        public void setPropertyEditorClass(Class<? extends PropertyEditor> clazz) {
            propertyEditorClass = clazz;
        }
    }

    /** A simple read/write property.
    * Subclasses should implement
    * {@link #getValue} and {@link #setValue}.
    */
    public abstract static class ReadWrite<T> extends PropertySupport<T> {
        /** Construct a new support.
        * @param name        the name of the property
        * @param type        the class type of the property
        * @param displayName the display name of the property
        * @param shortDescription a short description of the property
        */
        public ReadWrite(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription, true, true);
        }
    }

    private static final class FunctionalProperty<T> extends PropertySupport<T> {
        private final Supplier<T> supplier;
        private final Consumer<T> consumer;

        public FunctionalProperty(String name, Class<T> type, Supplier<T> supplier, Consumer<T> consumer) {
            super(name, type, null, null, supplier != null, consumer != null);
            this.supplier = supplier;
            this.consumer = consumer;
        }

        @Override
        public T getValue() throws IllegalAccessException {
            if (supplier != null) {
                return supplier.get();
            } else {
                throw new IllegalAccessException("Cannod read from WriteOnly property"); // NOI18N
            }
        }

        @Override
        public void setValue(T val) throws IllegalAccessException {
            if (consumer != null) {
                consumer.accept(val);
            } else {
                throw new IllegalAccessException("Cannot write to ReadOnly property"); // NOI18N
            }
        }
    }

    /** A simple read-only property.
    * Subclasses should implement {@link #getValue}.
    */
    public abstract static class ReadOnly<T> extends PropertySupport<T> {
        /** Construct a new support.
        * @param name        the name of the property
        * @param type        the class type of the property
        * @param displayName the display name of the property
        * @param shortDescription a short description of the property
        */
        public ReadOnly(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription, true, false);
        }

        /* Setter for the value.
        * @param val the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        @Override
        public void setValue(T val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            throw new IllegalAccessException("Cannot write to ReadOnly property"); // NOI18N
        }
    }

    /** A simple write-only property.
    * Subclasses should implement {@link #setValue}.
    */
    public abstract static class WriteOnly<T> extends PropertySupport<T> {
        /** Construct a new support.
        * @param name        the name of the property
        * @param type        the class type of the property
        * @param displayName the display name of the property
        * @param shortDescription a short description of the property
        */
        public WriteOnly(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription, false, true);
        }

        /* Getter for the value.
        * @return the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        @Override
        public T getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            throw new IllegalAccessException("Cannod read from WriteOnly property"); // NOI18N
        }
    }

    /** Support for the name property of a node. Delegates {@link #setValue} and {@link #getValue}
    * to {@link Node#setName} and {@link Node#getName}.
    */
    public static final class Name extends PropertySupport<String> {
        /** The node to which we delegate the work. */
        private final Node node;

        /** Create the name property for a node with the standard name and hint.
        * @param node the node
        */
        public Name(final Node node) {
            this(node, NbBundle.getMessage(PropertySupport.class, "CTL_StandardName"),
                NbBundle.getMessage(PropertySupport.class, "CTL_StandardHint")
            );
        }

        /** Create the name property for a node.
        * @param node the node
        * @param propName name of the "name" property
        * @param hint hint message for the "name" property
        */
        public Name(final Node node, final String propName, final String hint) {
            super(Node.PROP_NAME, String.class, propName, hint, true, node.canRename());
            this.node = node;
        }

        /* Getter for the value. Delegates to Node.getName().
        * @return the name
        */
        @Override
        public String getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return node.getName();
        }

        /* Setter for the value. Delegates to Node.setName().
        * @param val new name
        */
        @Override
        public void setValue(String val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Object oldName = node.getName();
            node.setName(val);
            node.firePropertyChange(Node.PROP_NAME, oldName, val);
        }
    }
     // end of Name inner class
}
