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
package org.netbeans.modules.php.editor.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.PhpElementImpl.Separator;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public final class ParameterElementImpl implements ParameterElement {

    private final String name;
    private final String defaultValue;
    private final String declaredType;
    private final String phpdocType;
    private final Set<TypeResolver> types;
    private final int offset;
    private final boolean isRawType;
    private final boolean isMandatory;
    private final boolean isReference;
    private final boolean isVariadic;
    private final boolean isUnionType;
    private final boolean isIntersectionType;
    private final int modifier;

    private boolean isMagicMethod = false;

    public ParameterElementImpl(
            final String name,
            final String defaultValue,
            final int offset,
            final String declaredType,
            final String phpdocType,
            final Set<TypeResolver> types,
            final boolean isMandatory,
            final boolean isRawType,
            final boolean isReference,
            final boolean isVariadic,
            final boolean isUnionType,
            final int modifier,
            final boolean isIntersectionType
    ) {
        this.name = name;
        this.isMandatory = isMandatory;
        this.defaultValue = (!isMandatory && defaultValue != null) ? decode(defaultValue) : ""; //NOI18N
        this.offset = offset;
        this.declaredType = declaredType;
        this.phpdocType = (phpdocType != null) ? decode(phpdocType) : null;
        this.types = types;
        this.isRawType = isRawType;
        this.isReference = isReference;
        this.isVariadic = isVariadic;
        this.isUnionType = isUnionType;
        this.isIntersectionType = isIntersectionType;
        this.modifier = modifier;
    }

    private ParameterElementImpl(Builder builder) {
        this.name = builder.name;
        this.isMandatory = builder.isMandatory;
        this.defaultValue = builder.defaultValue;
        this.offset = builder.offset;
        this.declaredType = builder.declaredType;
        this.phpdocType = builder.phpdocType;
        this.types = builder.types;
        this.isRawType = builder.isRawType;
        this.isReference = builder.isReference;
        this.isVariadic = builder.isVariadic;
        this.isUnionType = builder.isUnionType;
        this.isIntersectionType = builder.isIntersectionType;
        this.modifier = builder.modifier;
        this.isMagicMethod = builder.isMagicMethod;
    }

    static List<ParameterElement> parseParameters(final String signature) {
        List<ParameterElement> retval = new ArrayList<>();
        if (signature != null && signature.length() > 0) {
            final String regexp = String.format("\\%s", Separator.COMMA.toString()); //NOI18N
            for (String sign : signature.split(regexp)) {
                try {
                    final ParameterElement param = parseOneParameter(sign);
                    if (param != null) {
                        retval.add(param);
                    }
                } catch (NumberFormatException originalException) {
                    final String message = String.format("%s [for signature: %s]", originalException.getMessage(), signature); //NOI18N
                    final NumberFormatException formatException = new NumberFormatException(message);
                    formatException.initCause(originalException);
                    throw formatException;
                }
            }
        }
        return retval;
    }

    private static ParameterElement parseOneParameter(String sig) {
        ParameterElement retval = null;
        final String regexp = String.format("\\%s", Separator.COLON.toString()); //NOI18N
        String[] parts = sig.split(regexp);
        if (parts.length > 0) {
            String paramName = parts[0];
            Set<TypeResolver> types = TypeResolverImpl.parseTypes(parts[1]);
            boolean isRawType = Integer.parseInt(parts[2]) > 0;
            boolean isMandatory = Integer.parseInt(parts[4]) > 0;
            boolean isReference = Integer.parseInt(parts[5]) > 0;
            boolean isVariadic = Integer.parseInt(parts[6]) > 0;
            boolean isUnionType = Integer.parseInt(parts[7]) > 0;
            int modifier = Integer.parseInt(parts[8]);
            boolean isIntersectionType = Integer.parseInt(parts[9]) > 0;
            String defValue = parts.length > 3 ? parts[3] : null;
            String declaredType = parts.length > 10 ? parts[10] : null;
            String phpdocType = parts.length > 11 ? parts[11] : null;
            retval = new ParameterElementImpl(
                    paramName, defValue, -1, declaredType, phpdocType, types, isMandatory, isRawType, isReference, isVariadic, isUnionType, modifier, isIntersectionType);
        }
        return retval;
    }

    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        final String parameterName = getName().trim();
        assert parameterName.equals(encode(parameterName)) : parameterName;
        sb.append(parameterName).append(Separator.COLON);
        StringBuilder typeBuilder = new StringBuilder();
        // XXX just keep all types
        // note: dnf types are indexed as union types e.g. (X&Y)|Z -> X|Y|Z
        for (TypeResolver typeResolver : getTypes()) {
            TypeResolverImpl resolverImpl = (TypeResolverImpl) typeResolver;
            if (typeBuilder.length() > 0) {
                typeBuilder.append(Type.getTypeSeparator(isIntersectionType));
            }
            typeBuilder.append(resolverImpl.getSignature());
        }
        String typeSignatures = typeBuilder.toString().trim();
        sb.append(typeSignatures);
        sb.append(Separator.COLON);
        sb.append(isRawType ? 1 : 0);
        sb.append(Separator.COLON);
        if (!isMandatory()) {
            final String defVal = getDefaultValue();
            assert defVal != null;
            sb.append(encode(defVal));
        }
        sb.append(Separator.COLON);
        sb.append(isMandatory ? 1 : 0);
        sb.append(Separator.COLON);
        sb.append(isReference ? 1 : 0);
        sb.append(Separator.COLON);
        sb.append(isVariadic ? 1 : 0);
        sb.append(Separator.COLON);
        sb.append(isUnionType ? 1 : 0);
        sb.append(Separator.COLON);
        sb.append(modifier);
        sb.append(Separator.COLON);
        sb.append(isIntersectionType ? 1 : 0);
        sb.append(Separator.COLON);
        sb.append((declaredType != null) ? declaredType : ""); // NOI18N
        sb.append(Separator.COLON);
        // PhpDoc may have separators(":", ";", ",")
        // e.g. @param (callable(CacheItemInterface,bool):T)|(callable(ItemInterface,bool):T)|CallbackInterface<T>
        sb.append((phpdocType != null) ? encode(phpdocType) : ""); // NOI18N
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<TypeResolver> getTypes() {
        // use LinkedHashSet to keep the type order
        // avoid being changed type order(e.g. int|float|Foo|Bar) when an override method is generated
        return new LinkedHashSet<>(types);
    }

    @CheckForNull
    @Override
    public String getDeclaredType() {
        return declaredType;
    }

    @CheckForNull
    @Override
    public String getPhpdocType() {
        return phpdocType;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean hasDeclaredType() {
        return isRawType;
    }

    @Override
    public boolean isMandatory() {
        return isMandatory;
    }

    static String encode(String inStr) {
        return encode(inStr, Separator.toEnumSet());
    }
    static String encode(String inStr, final EnumSet<Separator> separators) {
        StringBuilder outStr = new StringBuilder(6 * inStr.length());

        for (int i = 0; i < inStr.length(); i++) {
            final char charAt = inStr.charAt(i);
            boolean encode = isEncodedChar(i, inStr);
            if (!encode) {
                for (Separator separator : separators) {
                    char separatorChar = separator.toString().charAt(0);
                    if (charAt == separatorChar) {
                        encode = true;
                        break;
                    }
                }
            }
            if (encode) {
                outStr.append(encodeChar(inStr.charAt(i)));
                continue;
            }

            outStr.append(inStr.charAt(i));
        }

        return outStr.toString();
    }

    private static String encodeChar(char ch) {
        String encChar = Integer.toString((int) ch, 16);

        return "\\u" + "0000".substring(0, "0000".length() - encChar.length()).concat(encChar); // NOI18N
    }

    private static String decode(final String inStr) {
        StringBuilder outStr = new StringBuilder(inStr.length());

        try {
            for (int i = 0; i < inStr.length(); i++) {
                if (isEncodedChar(i, inStr)) {
                    String decChar = inStr.substring(i + 2, i + 6);
                    outStr.append((char) Integer.parseInt(decChar, 16));
                    i += 5;
                } else {
                    outStr.append(inStr.charAt(i));
                }
            }
        } catch (NumberFormatException e) {
            Exceptions.printStackTrace(e);

            return inStr;
        }

        return outStr.toString();
    }

    private static boolean isEncodedChar(final int currentPosition, final String inStr) {
        boolean isEncodedChar = (currentPosition + 5) < inStr.length();

        if (isEncodedChar) {
            isEncodedChar &= ((inStr.charAt(currentPosition) == '\\')
                    && (inStr.charAt(currentPosition + 1) == 'u'));

            for (int i = currentPosition + 2; isEncodedChar && (i < (currentPosition + 6)); i++) {
                char c = inStr.charAt(i);
                isEncodedChar &= (Character.digit(c, 16) != -1);
            }
        }

        return isEncodedChar;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String signature = sb.toString();
            try {
                ParameterElement parsedParameter = parseOneParameter(signature);
                assert getName().equals(parsedParameter.getName()) : signature;
                assert hasDeclaredType() == parsedParameter.hasDeclaredType() : signature;
                String defValue = getDefaultValue();
                if (defValue != null) {
                    String paramDefaultValue = parsedParameter.getDefaultValue();
                    assert paramDefaultValue != null && defValue.equals(paramDefaultValue) : signature;
                }
                assert isMandatory() == parsedParameter.isMandatory() : signature;
                assert isReference() == parsedParameter.isReference() : signature;
                assert isVariadic() == parsedParameter.isVariadic() : signature;
                assert isUnionType() == parsedParameter.isUnionType() : signature;
                assert getModifier() == parsedParameter.getModifier() : signature;
                assert isIntersectionType() == parsedParameter.isIntersectionType() : signature;
                String declType = getDeclaredType();
                if (declType != null) {
                    String paramDeclaredType = parsedParameter.getDeclaredType();
                    assert paramDeclaredType != null && declType.equals(paramDeclaredType) : signature;
                }
                String docType = getPhpdocType();
                if (docType != null) {
                    String paramPhpDocType = parsedParameter.getPhpdocType();
                    assert paramPhpDocType != null && docType.equals(paramPhpDocType) : "signature:" + signature + ", paramPhpDocType: " + paramPhpDocType + ", docType: " + docType; // NOI18N
                }
            } catch (NumberFormatException originalException) {
                final String message = String.format("%s [for signature: %s]", originalException.getMessage(), signature); //NOI18N
                final NumberFormatException formatException = new NumberFormatException(message);
                formatException.initCause(originalException);
                throw formatException;
            }
        }
    }

    @Override
    public OffsetRange getOffsetRange() {
        int endOffset = getOffset() + getName().length();
        return new OffsetRange(offset, endOffset);
    }

    @Override
    public String asString(OutputType outputType) {
        return asString(outputType, TypeNameResolverImpl.forNull());
    }

    @Override
    public String asString(OutputType outputType, TypeNameResolver typeNameResolver) {
        return asString(outputType, typeNameResolver, null);
    }

    @Override
    public String asString(OutputType outputType, TypeNameResolver typeNameResolver, @NullAllowed PhpVersion phpVersion) {
        StringBuilder sb = new StringBuilder();
        Set<TypeResolver> typesResolvers = getTypes();
        boolean forDeclaration = outputType == OutputType.COMPLETE_DECLARATION
                || outputType == OutputType.COMPLETE_DECLARATION_WITH_MODIFIER
                || outputType == OutputType.SHORTEN_DECLARATION
                || outputType == OutputType.SHORTEN_DECLARATION_WITH_MODIFIER;

        if (outputType == OutputType.COMPLETE_DECLARATION_WITH_MODIFIER
                || outputType == OutputType.SHORTEN_DECLARATION_WITH_MODIFIER) {
            String modifierString = BodyDeclaration.Modifier.toString(modifier);
            if (modifierString != null && !modifierString.isEmpty()) {
                // [NETBEANS-4443] PHP 8.0 Constructor Property Promotion
                sb.append(modifierString).append(" "); // NOI18N
            }
        }
        if (forDeclaration && hasDeclaredType()) {
            if (StringUtils.hasText(getDeclaredType())) {
                String[] splitTypes = Type.splitTypes(getDeclaredType());
                List<String> resolvedTypes = new ArrayList<>();
                if (isMagicMethod && phpVersion != null) {
                    String validType = MethodElementImpl.getValidType(getDeclaredType(), phpVersion);
                    if (StringUtils.hasText(validType)) {
                        sb.append(validType).append(' ');
                    }
                } else if (splitTypes.length == typesResolvers.size()) {
                    String template = Type.toTypeTemplate(getDeclaredType());
                    for (TypeResolver typeResolver : typesResolvers) {
                        resolvedTypes.add(typeNameResolver.resolve(typeResolver.getTypeName(false)).toString());
                    }
                    sb.append(String.format(template, resolvedTypes.toArray(new String[0]))).append(' ');
                } else {
                    sb.append(getDeclaredType()).append(' ');
                }
            }
        }
        if (forDeclaration) {
            if (isReference()) {
                sb.append(VariableElementImpl.REFERENCE_PREFIX);
            }
            if (isVariadic()) {
                sb.append(VariableElementImpl.VARIADIC_PREFIX);
            }
        }
        sb.append(getName());
        if (forDeclaration) {
            String defVal = getDefaultValue();
            if (!isMandatory() && StringUtils.hasText(defVal)) {
                sb.append(" = "); //NOI18N
                if (outputType == OutputType.COMPLETE_DECLARATION
                        || outputType == OutputType.COMPLETE_DECLARATION_WITH_MODIFIER) {
                    sb.append(defVal);
                } else {
                    sb.append(defVal.length() > 20 ? "..." : defVal); //NOI18N
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isReference() {
        return isReference;
    }

    @Override
    public boolean isVariadic() {
        return isVariadic;
    }

    @Override
    public boolean isUnionType() {
        return isUnionType;
    }

    @Override
    public int getModifier() {
        return modifier;
    }

    @Override
    public boolean isIntersectionType() {
        return isIntersectionType;
    }

    //~ inner class
    static class Builder {

        private final String name;
        private String defaultValue = null;
        private String declaredType = null;
        private String phpdocType = null;
        private Set<TypeResolver> types = Collections.emptySet();
        private int offset = 0;
        private int modifier = 0;
        private boolean isRawType = false;
        private boolean isMandatory = false;
        private boolean isReference = false;
        private boolean isVariadic = false;
        private boolean isUnionType = false;
        private boolean isIntersectionType = false;
        private boolean isMagicMethod = false; // this is false as a field of ParameterElementImpl by default

        public Builder(String name) {
            this.name = name;
        }

        public ParameterElement build() {
            return new ParameterElementImpl(this);
        }

        public Builder defaultValue(@NullAllowed String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder declaredType(@NullAllowed String declaredType) {
            this.declaredType = declaredType;
            return this;
        }

        public Builder setPhpdocType(String phpdocType) {
            this.phpdocType = phpdocType;
            return this;
        }

        public Builder setTypes(Set<TypeResolver> types) {
            this.types = new HashSet<>(types);
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder isRawType(boolean isRawType) {
            this.isRawType = isRawType;
            return this;
        }

        public Builder isMandatory(boolean isMandatory) {
            this.isMandatory = isMandatory;
            return this;
        }

        public Builder isReference(boolean isReference) {
            this.isReference = isReference;
            return this;
        }

        public Builder isVariadic(boolean isVariadic) {
            this.isVariadic = isVariadic;
            return this;
        }

        public Builder isUnionType(boolean isUnionType) {
            this.isUnionType = isUnionType;
            return this;
        }

        public Builder isIntersectionType(boolean isIntersectionType) {
            this.isIntersectionType = isIntersectionType;
            return this;
        }

        public Builder modifier(int modifier) {
            this.modifier = modifier;
            return this;
        }

        public Builder isMagicMethod(boolean isMagicMethod) {
            this.isMagicMethod = isMagicMethod;
            return this;
        }
    }
}
