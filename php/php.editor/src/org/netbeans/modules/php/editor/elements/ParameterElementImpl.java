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
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
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
    private final Set<TypeResolver> types;
    private final int offset;
    private final boolean isRawType;
    private final boolean isMandatory;
    private final boolean isReference;
    private final boolean isVariadic;
    private final boolean isUnionType;
    private final int modifier;

    public ParameterElementImpl(
            final String name,
            final String defaultValue,
            final int offset,
            final Set<TypeResolver> types,
            final boolean isMandatory,
            final boolean isRawType,
            final boolean isReference,
            final boolean isVariadic,
            final boolean isUnionType,
            final int modifier) {
        this.name = name;
        this.isMandatory = isMandatory;
        this.defaultValue = (!isMandatory && defaultValue != null) ? decode(defaultValue) : ""; //NOI18N
        this.offset = offset;
        this.types = types;
        this.isRawType = isRawType;
        this.isReference = isReference;
        this.isVariadic = isVariadic;
        this.isUnionType = isUnionType;
        this.modifier = modifier;
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
            String defValue = parts.length > 3 ? parts[3] : null;
            retval = new ParameterElementImpl(
                    paramName, defValue, -1, types, isMandatory, isRawType, isReference, isVariadic, isUnionType, modifier);
        }
        return retval;
    }

    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        final String parameterName = getName().trim();
        assert parameterName.equals(encode(parameterName)) : parameterName;
        sb.append(parameterName).append(Separator.COLON);
        StringBuilder typeBuilder = new StringBuilder();
        for (TypeResolver typeResolver : getTypes()) {
            TypeResolverImpl resolverImpl = (TypeResolverImpl) typeResolver;
            if (typeBuilder.length() > 0) {
                typeBuilder.append(Separator.PIPE);
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
            if (isUnionType) {
                boolean firstType = true;
                for (TypeResolver typeResolver : typesResolvers) {
                    if (typeResolver.isResolved()) {
                        if (firstType) {
                            firstType = false;
                        } else {
                            sb.append(Type.SEPARATOR);
                        }
                        sb.append(typeNameResolver.resolve(typeResolver.getTypeName(false)));
                    }
                }
                sb.append(' ');
            } else if (typesResolvers.size() > 1 && !isUnionType) {
                sb.append(Type.MIXED).append(' ');
            } else {
                for (TypeResolver typeResolver : typesResolvers) {
                    if (typeResolver.isResolved()) {
                        if (typeResolver.isNullableType()) {
                            sb.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                        }
                        sb.append(typeNameResolver.resolve(typeResolver.getTypeName(false))).append(' '); //NOI18N
                        break;
                    }
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
}
