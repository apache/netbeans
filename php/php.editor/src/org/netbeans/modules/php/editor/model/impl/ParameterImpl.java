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

package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.Parameter;
import org.openide.util.Exceptions;

/**
 *
 * @author Radek Matous
 */
public class ParameterImpl implements Parameter {
    private final String name;
    private final String defaultValue;
    private final List<QualifiedName> types;
    private final OffsetRange range;
    private final boolean isRawType;
    private final boolean isReference;
    private final boolean isVariadic;
    private final boolean isUnionType;

    public ParameterImpl(
            String name,
            String defaultValue,
            List<QualifiedName> types,
            boolean isRawType,
            OffsetRange range,
            boolean isReference,
            boolean isVariadic,
            boolean isUnionType) {
        this.name = name;
        this.defaultValue = defaultValue;
        if (types == null) {
            this.types = Collections.emptyList();
        } else {
            this.types = types;
        }
        this.range = range;
        this.isRawType = isRawType;
        this.isReference = isReference;
        this.isVariadic = isVariadic;
        this.isUnionType = isUnionType;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isMandatory() {
        return defaultValue == null;
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
    public List<QualifiedName> getTypes() {
        return new ArrayList<>(types);
    }

    @Override
    public OffsetRange getOffsetRange() {
        return range;
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(":"); //NOI18N
        List<QualifiedName> qNames = getTypes();
        for (int idx = 0; idx < qNames.size(); idx++) {
            if (idx > 0) {
                sb.append('|'); //NOI18N
            }
            QualifiedName qualifiedName = qNames.get(idx);
            sb.append(qualifiedName.toString());
        }
        sb.append(":"); //NOI18N
        sb.append(isRawType ? 1 : 0);
        sb.append(":"); //NOI18N
        String defValue = getDefaultValue();
        sb.append(encode(defValue));
        sb.append(":"); //NOI18N
        sb.append(isReference ? 1 : 0);
        sb.append(":"); //NOI18N
        sb.append(isVariadic ? 1 : 0);
        sb.append(":"); //NOI18N
        sb.append(isUnionType ? 1 : 0);
        return sb.toString();
    }

    public static List<Parameter> toParameters(String args) {
        List<Parameter> parameters = new ArrayList<>();
        if (args != null && args.length() > 0) {
            String[] pams = args.split("\\,");
            for (String par : pams) {
                String[] parts = par.split("\\:");
                if (parts.length > 0) {
                    String paramName = parts[0];
                    List<QualifiedName> qualifiedNames = new ArrayList<>();
                    if (parts.length > 1) {
                        String typenames = parts[1];
                        String[] splittedTypes = typenames.length() > 0 ? typenames.split("\\|") : new String[0];
                        for (String type : splittedTypes) {
                            qualifiedNames.add(QualifiedName.create(type));
                        }
                    }
                    boolean isRawType = Integer.parseInt(parts[2]) > 0;
                    String defValue = (parts.length > 3) ? parts[3] : "";
                    boolean isReference = Integer.parseInt(parts[4]) > 0;
                    boolean isVariadic = Integer.parseInt(parts[5]) > 0;
                    boolean isUnionType = Integer.parseInt(parts[6]) > 0;
                    parameters.add(new ParameterImpl(
                            paramName,
                            (defValue.length() != 0) ? decode(defValue) : null,
                            qualifiedNames,
                            isRawType,
                            OffsetRange.NONE,
                            isReference,
                            isVariadic,
                            isUnionType));
                }
            }
        }
        return parameters;
    }

    private static String encode(String inStr) {
        StringBuilder outStr = new StringBuilder(6 * inStr.length());

        for (int i = 0; i < inStr.length(); i++) {
            if ((inStr.charAt(i) == ':') || (inStr.charAt(i) == '|') || //NOI18N
                    (inStr.charAt(i) == ';') || (inStr.charAt(i) == ',') ||  isEncodedChar(i, inStr)) { //NOI18N
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
            isEncodedChar &= ((inStr.charAt(currentPosition) == '\\') && (inStr.charAt(currentPosition + 1) == 'u'));
            for (int i = currentPosition + 2; isEncodedChar && (i < (currentPosition + 6)); i++) {
                char c = inStr.charAt(i);
                isEncodedChar &= (Character.digit(c, 16) != -1);
            }
        }

        return isEncodedChar;
    }

    @Override
    public boolean hasRawType() {
        return isRawType;
    }

}
