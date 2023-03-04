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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.*;
import org.openide.util.Parameters;

/**
 * Support class for parsing annotations elements. An instance of this class
 * can be configured with the names and types of the expected elements of an
 * {@link AnnotationMirror}, together with their default values. Then an
 * annotation mirror is passed to the {@link #parse} method, which will
 * return the parsed values for each element that was present in the annotation
 * mirror, or the default value for that element if it was not present.
 *
 * <p>For example, in order to parse the following annotation:
 *
 * <pre>
 * \@interface MyAnnotation {
 *     String stringValue();
 *     int intValue();
 * }
 * </pre>
 *
 * the following code could be used:
 *
 * <pre>
 * // helper is an AnnotationModelHelper
 * AnnotationParser parser = AnnotationParser.create(helper);
 * parser.expectStringValue("stringValue", parser.defaultValue(""));
 * parser.expectPrimitiveValue("intValue", Integer.class, parser.defaultValue(42));
 * // annotation is an AnnotationMirror
 * ParseResult parseResult = parser.parse(annotation);
 * </pre>
 *
 * Then the element values can be obtained from {@link ParseResult}:
 *
 * <pre>
 * String stringValue = parseResult.get("stringValue", String.class);
 * int intValue = parseResult.get("intValue", Integer.class);
 * </pre>
 *
 * <code>AnnotationParser</code> contains <code>expect...</code> methods for all
 * types of annotation elements: primitive values, string, classes, enum constants, annotations
 * and arrays thereof.
 *
 * @author Andrei Badea
 */
public final class AnnotationParser {

    private static final Set<Class<?>> PRIMITIVE_WRAPPERS = new HashSet<Class<?>>();

    private final Map<String, ValueProvider> providers = new HashMap<String, ValueProvider>();
    private final AnnotationHelper helper;

    static {
        PRIMITIVE_WRAPPERS.add(Boolean.class);
        PRIMITIVE_WRAPPERS.add(Byte.class);
        PRIMITIVE_WRAPPERS.add(Short.class);
        PRIMITIVE_WRAPPERS.add(Integer.class);
        PRIMITIVE_WRAPPERS.add(Long.class);
        PRIMITIVE_WRAPPERS.add(Character.class);
        PRIMITIVE_WRAPPERS.add(Float.class);
        PRIMITIVE_WRAPPERS.add(Double.class);
    }

    /**
     * Creates a {@link DefaultProvider} for the given defaultValue.
     */
    public static DefaultProvider defaultValue(Object defaultValue) {
        return new DefaultProviderImpl(defaultValue);
    }

    /**
     * Creates a new <code>AnnotationParser</code>.
     */
    public static AnnotationParser create(AnnotationModelHelper helper) {
        Parameters.notNull("helper", helper); // NOI18N
        return new AnnotationParser(helper.getHelper());
    }
    
    public static AnnotationParser create(AnnotationHelper helper) {
        Parameters.notNull("helper", helper); // NOI18N
        return new AnnotationParser(helper);
    }

    private AnnotationParser(AnnotationHelper helper) {
        this.helper = helper;
    }

    /**
     * States that the parsed annotation contains a primitive type element. The
     * value of the element will be available in the parse result as a primitive
     * wrapper class (<code>Integer</code>, etc.).
     *
     * @param  name the element name.
     * @param  type a primitive wrapper class specifying the element type.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public <T> void expectPrimitive(String name, Class<T> type, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("type", type); // NOI18N
        if (!PRIMITIVE_WRAPPERS.contains(type)) {
            throw new IllegalArgumentException(type + " is not a primitive wrapper class"); // NOI18N
        }
        addProvider(name, new PrimitiveValueProvider(type, defaultProvider));
    }

    /**
     * States that the parsed annotation contains a <code>String</code> element. The
     * value of the element will be available in the parse result as a <code>String</code>
     * value.
     *
     * @param  name the element name.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectString(String name, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        addProvider(name, new PrimitiveValueProvider(String.class, defaultProvider));
    }

    /**
     * States that the parsed annotation contains a class element. The value of the
     * element will be available in the parse result as a <code>String</code>
     * containing the fully-qualified name of the class.
     *
     * @param  name the element name.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectClass(String name, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        addProvider(name, new ClassValueProvider(defaultProvider));
    }

    /**
     * States that the parsed annotation contains an enum constant element. The value
     * of the element will be available in the parse result as a <code>String</code>
     * containing the name of the enum constant.
     *
     * @param  name the element name.
     * @param  enumType a {@link TypeMirror} specifying the enum type.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectEnumConstant(String name, TypeMirror enumType, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("enumType", enumType); // NOI18N
        addProvider(name, new EnumConstantValueProvider(enumType, defaultProvider));
    }

    /**
     * States that the parsed annotation contains an annotation element. The value
     * of the element will be available in the parse result as the result
     * of the invocation of the <code>AnnotationValueHandler</code> passed in the
     * <code>handler</code> parameter.
     *
     * @param  name the element name.
     * @param  annotationType a {@link TypeMirror} specifying the annotation type.
     * @param  handler an {@link AnnotationValueHandler} which will be invoked to compute
     *         and return a value that represents the element.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectAnnotation(String name, TypeMirror annotationType, AnnotationValueHandler handler, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("annotationType", annotationType); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        addProvider(name, new AnnotationValueProvider(annotationType, handler, defaultProvider));
    }

    /**
     * States that the parsed annotation contains a primitive array element. The value
     * of the element will be available in the parse result as the result
     * of the invocation of the <code>ArrayValueHandler</code> passed in the
     * <code>handler</code> parameter.
     *
     * @param  name the element name.
     * @param  type a primitive wrapper class specifying the type of the array members.
     * @param  handler an {@link ArrayValueHandler} which will be invoked to compute
     *         and return a value that represents the element.Parameters.notNull("enumType", enumType); // NOI18N
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectPrimitiveArray(String name, Class<?> type, ArrayValueHandler handler, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("type", type); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        addProvider(name, new PrimitiveArrayValueProvider(type, handler, defaultProvider));
    }

    /**
     * States that the parsed annotation contains a <code>String</code> array element. The value
     * of the element will be available in the parse result as the result
     * of the invocation of the <code>ArrayValueHandler</code> passed in the
     * <code>handler</code> parameter.
     *
     * @param  name the element name.
     * @param  handler an {@link ArrayValueHandler} which will be invoked to compute
     *         and return a value that represents the element.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectStringArray(String name, ArrayValueHandler handler, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        addProvider(name, new PrimitiveArrayValueProvider(String.class, handler, defaultProvider));
    }

    /**
     * States that the parsed annotation contains a class array element. The value
     * of the element will be available in the parse result as the result
     * of the invocation of the <code>ArrayValueHandler</code> passed in the
     * <code>handler</code> parameter.
     *
     * @param  name the element name.
     * @param  handler an {@link ArrayValueHandler} which will be invoked to compute
     *         and return a value that represents the element.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectClassArray(String name, ArrayValueHandler handler, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        addProvider(name, new ClassArrayValueProvider(handler, defaultProvider));
    }

    /**
     * States that the parsed annotation contains an enum constant array element. The value
     * of the element will be available in the parse result as the result
     * of the invocation of the <code>ArrayValueHandler</code> passed in the
     * <code>handler</code> parameter.
     *
     * @param  name the element name.
     * @param  enumType a {@link TypeMirror} specifying the type of the array members.
     * @param  handler an {@link ArrayValueHandler} which will be invoked to compute
     *         and return a value that represents the element.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectEnumConstantArray(String name, TypeMirror enumType, ArrayValueHandler handler, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("enumType", enumType); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        addProvider(name, new EnumConstantArrayValueProvider(enumType, handler, defaultProvider));
    }

    /**
     * States that the parsed annotation contains an annotation array element. The value
     * of the element will be available in the parse result as the result
     * of the invocation of the <code>ArrayValueHandler</code> passed in the
     * <code>handler</code> parameter.
     *
     * @param  name the element name.
     * @param  annotationType a {@link TypeMirror} specifying the type of the array members.
     * @param  handler an {@link ArrayValueHandler} which will be invoked to compute
     *         and return a value that represents the element.
     * @param  defaultProvider a {@link DefaultProvider} for the default value or null.
     */
    public void expectAnnotationArray(String name, TypeMirror annotationType, ArrayValueHandler handler, DefaultProvider defaultProvider) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("annotationType", annotationType); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        addProvider(name, new AnnotationArrayValueProvider(annotationType, handler, defaultProvider));
    }

    /**
     * States that the given element in the parsed annotation should be parsed
     * by the given {@link ValueProvider}.
     *
     * @param  name the element name.
     * @param  provider the <code>ValueProvider</code> to use to parse the
     *         annotation element.
     */
    public void expect(String name, ValueProvider provider) {
        Parameters.notNull("name", name); // NOI18N
        addProvider(name, provider);
    }

    /**
     * Parses the given annotation.
     *
     * @param  annotation the annotation to be parsed; can be null.
     * @return the parse result; never null.
     */
    public ParseResult parse(AnnotationMirror annotation) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (annotation != null) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> elementEntry : annotation.getElementValues().entrySet()) {
                ExecutableElement element = elementEntry.getKey();
                String name = element.getSimpleName().toString();
                ValueProvider provider = providers.get(name);
                if (provider != null) {
                    Object value = provider.getValue(elementEntry.getValue());
                    if (value != null) {
                        result.put(name, value);
                    }
                }
            }
        }
        for (Map.Entry<String, ValueProvider> providerEntry : providers.entrySet()) {
            String name = providerEntry.getKey();
            if (!result.containsKey(name)) {
                Object value = providerEntry.getValue().getDefaultValue();
                if (value != null) {
                    result.put(name, value);
                }
            }
        }
        return new ParseResult(result);
    }

    private void addProvider(String name, ValueProvider provider) {
        if (providers.containsKey(name)) {
            throw new IllegalArgumentException("There is already a provider for element name '" + name + "'"); // NOI18N
        }
        providers.put(name, provider);
    }

    /**
     * Simple <code>DefaultProvider</code> implementation.
     */
    private static final class DefaultProviderImpl implements DefaultProvider {

        private final Object defaultValue;

        public DefaultProviderImpl(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }

    /**
     * Abstract <code>ValueProvider</code> which delegates its
     * <code>getDefaultValue()</code> method to a given <code>DefaultProvider</code>.
     */
    private abstract class DefaultValueProvider implements ValueProvider {

        private final DefaultProvider defaultProvider;

        public DefaultValueProvider(DefaultProvider defaultProvider) {
            this.defaultProvider = defaultProvider;
        }

        public Object getDefaultValue() {
            return defaultProvider != null ? defaultProvider.getDefaultValue() : null;
        }
    }

    /**
     * Abstract <code>ValueProvider</code> for annotation elements which
     * need to check the element type.
     */
    private abstract class TypeCheckingValueProvider extends DefaultValueProvider {

        private final TypeMirror typeToCheck;

        public TypeCheckingValueProvider(TypeMirror typeToCheck, DefaultProvider defaultProvider) {
            super(defaultProvider);
            this.typeToCheck = typeToCheck;
        }

        protected boolean isSameAsTypeToCheck(TypeMirror otherType) {
            return helper.getCompilationInfo().getTypes().isSameType(typeToCheck, 
                    otherType);
        }
    }

    /**
     * Abstract <code>ValueProvider</code> for arrays.
     */
    private abstract class DefaultArrayValueProvider extends DefaultValueProvider {

        private final ArrayValueHandler handler;

        public DefaultArrayValueProvider(ArrayValueHandler handler, DefaultProvider defaultProvider) {
            super(defaultProvider);
            this.handler = handler;
        }

        public Object getValue(AnnotationValue elementValue) {
            Object value = elementValue.getValue();
            if (value instanceof List) {
                @SuppressWarnings("unchecked") // NOI18N
                List<AnnotationValue> arrayMembers = (List<AnnotationValue>)value;
                if (checkMembers(arrayMembers)) {
                    Object result = handler.handleArray(arrayMembers);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return getDefaultValue();
        }

        protected abstract boolean checkMembers(List<AnnotationValue> arrayMembers);
    }

    /**
     * Abstract <code>ValueProvider</code> for arrays whose member types need
     * to be checked.
     */
    private abstract class TypeCheckingArrayValueProvider extends TypeCheckingValueProvider {

        private final ArrayValueHandler handler;

        public TypeCheckingArrayValueProvider(TypeMirror typeToCheck, ArrayValueHandler handler, DefaultProvider defaultProvider) {
            super(typeToCheck, defaultProvider);
            this.handler = handler;
        }

        public Object getValue(AnnotationValue elementValue) {
            Object value = elementValue.getValue();
            if (value instanceof List) {
                @SuppressWarnings("unchecked") // NOI18N
                List<AnnotationValue> arrayMembers = (List<AnnotationValue>)value;
                if (checkMembers(arrayMembers)) {
                    Object result = handler.handleArray(arrayMembers);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return getDefaultValue();
        }

        protected abstract boolean checkMembers(List<AnnotationValue> arrayMembers);
    }

    /**
     * <code>ValueProvider</code> for primitive and <code>String</code> elements.
     */
    private class PrimitiveValueProvider extends DefaultValueProvider {

        private final Class<?> type;

        public PrimitiveValueProvider(Class<?> type, DefaultProvider defaultProvider) {
            super(defaultProvider);
            this.type = type;
        }

        public Object getValue(AnnotationValue elementValue) {
            Object value = elementValue.getValue();
            if (type.isInstance(value)) {
                return value;
            }
            return getDefaultValue();
        }
    }

    /**
     * <code>ValueProvider</code> for class elements.
     */
    private final class ClassValueProvider extends DefaultValueProvider {

        public ClassValueProvider(DefaultProvider defaultProvider) {
            super(defaultProvider);
        }

        public Object getValue(AnnotationValue elementValue) {
            Object value = elementValue.getValue();
            if (value instanceof TypeMirror) {
                TypeMirror type = (TypeMirror)value;
                if (TypeKind.DECLARED == type.getKind()) {
                    return ((TypeElement)(((DeclaredType)value).asElement())).getQualifiedName().toString();
                }
            }
            return getDefaultValue();
        }
    }

    /**
     * <code>ValueProvider</code> for enum constant elements.
     */
    private final class EnumConstantValueProvider extends TypeCheckingValueProvider {

        public EnumConstantValueProvider(TypeMirror enumType, DefaultProvider defaultProvider) {
            super(enumType, defaultProvider);
        }

        public Object getValue(AnnotationValue elementValue) {
            Object value = elementValue.getValue();
            if (value instanceof VariableElement) {
                VariableElement field = (VariableElement)value;
                TypeMirror enumType = field.getEnclosingElement().asType();
                if (isSameAsTypeToCheck(enumType)) {
                    return field.getSimpleName().toString();
                }
            }
            return getDefaultValue();
        }
    }

    /**
     * <code>ValueProvider</code> for annotation elements.
     */
    private final class AnnotationValueProvider extends TypeCheckingValueProvider {

        private final AnnotationValueHandler handler;

        public AnnotationValueProvider(TypeMirror annotationType, AnnotationValueHandler handler, DefaultProvider defaultProvider) {
            super(annotationType, defaultProvider);
            this.handler = handler;
        }

        public Object getValue(AnnotationValue elementValue) {
            Object value = elementValue.getValue();
            if (value instanceof AnnotationMirror) {
                AnnotationMirror annotation = (AnnotationMirror)value;
                if (isSameAsTypeToCheck(annotation.getAnnotationType())) {
                    Object result = handler.handleAnnotation(annotation);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return getDefaultValue();
        }
    }

    /**
     * <code>ValueProvider</code> for arrays of primitive types and <code>String</code>s'.
     */
    private final class PrimitiveArrayValueProvider extends DefaultArrayValueProvider {

        private final Class<?> type;

        public PrimitiveArrayValueProvider(Class<?> type, ArrayValueHandler handler, DefaultProvider defaultProvider) {
            super(handler, defaultProvider);
            this.type = type;
        }

        protected boolean checkMembers(List<AnnotationValue> arrayMembers) {
            for (AnnotationValue arrayMember : arrayMembers) {
                Object value = arrayMember.getValue();
                if (!type.isInstance(value)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * <code>ValueProvider</code> for arrays of classes.
     */
    private final class ClassArrayValueProvider extends DefaultArrayValueProvider {

        public ClassArrayValueProvider(ArrayValueHandler handler, DefaultProvider defaultProvider) {
            super(handler, defaultProvider);
        }

        protected boolean checkMembers(List<AnnotationValue> arrayMembers) {
            for (AnnotationValue arrayMember : arrayMembers) {
                Object value = arrayMember.getValue();
                if (!(value instanceof TypeMirror)) {
                    return false;
                }
                TypeMirror type = (TypeMirror)value;
                if (TypeKind.DECLARED != type.getKind()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * <code>ValueProvider</code> for arrays of enum constants.
     */
    private final class EnumConstantArrayValueProvider extends TypeCheckingArrayValueProvider {

        public EnumConstantArrayValueProvider(TypeMirror enumType, ArrayValueHandler handler, DefaultProvider defaultProvider) {
            super(enumType, handler, defaultProvider);
        }

        protected boolean checkMembers(List<AnnotationValue> arrayMembers) {
            for (AnnotationValue arrayMember : arrayMembers) {
                Object value = arrayMember.getValue();
                if (!(value instanceof VariableElement)) {
                    return false;
                }
                VariableElement field = (VariableElement)value;
                TypeMirror enumType = field.getEnclosingElement().asType();
                if (!isSameAsTypeToCheck(enumType)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * <code>ValueProvider</code> for arrays of annotations.
     */
    private final class AnnotationArrayValueProvider extends TypeCheckingArrayValueProvider {

        public AnnotationArrayValueProvider(TypeMirror annotationType, ArrayValueHandler handler, DefaultProvider defaultProvider) {
            super(annotationType, handler, defaultProvider);
        }

        protected boolean checkMembers(List<AnnotationValue> arrayMembers) {
            for (AnnotationValue arrayMember : arrayMembers) {
                Object value = arrayMember.getValue();
                if (!(value instanceof AnnotationMirror)) {
                    return false;
                }
                AnnotationMirror annotation = (AnnotationMirror)value;
                if (!isSameAsTypeToCheck(annotation.getAnnotationType())) {
                    return false;
                }
            }
            return true;
        }
    }
}
