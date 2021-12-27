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
package org.netbeans.modules.java.nativeimage.debugger.displayer;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.nativeimage.api.debug.EvaluateException;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer;

/**
 *
 * @author martin
 */
public final class JavaVariablesDisplayer implements VariableDisplayer {

    private static final String HUB = "__hub__";
    private static final String ARRAY = "__array__";
    private static final String ARRAY_LENGTH = "__length__";
    private static final String COMPRESSED_REF_REFIX = "_z_.";
    private static final String PUBLIC = "public";
    private static final String STRING_VALUE = "value";
    private static final String STRING_CODER = "coder";
    private static final String HASH = "hash";
    private static final String UNSET = "<optimized out>";

    private static final String[] STRING_TYPES = new String[] { String.class.getName(), StringBuilder.class.getName(), StringBuffer.class.getName() };

    private NIDebugger debugger;

    public JavaVariablesDisplayer() {
    }

    public void setDebugger(NIDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public NIVariable[] displayed(NIVariable[] variables) {
        List<NIVariable> displayedVars = new ArrayList<>(variables.length);
        for (NIVariable var : variables) {
           String value = var.getValue();
            if (UNSET.equals(value)) {
                continue;
            }
            int nch = var.getNumChildren();
            NIVariable displayedVar;
            if (nch == 0) {
                String name = var.getName();
                if (!name.equals(getNameOrIndex(name))) {
                    displayedVar = new Var(var);
                } else {
                    displayedVar = var;
                }
            } else {
                NIVariable[] children = var.getChildren();
                NIVariable[] subChildren = children[0].getChildren();
                // Check for Array
                if (subChildren.length == 3 &&
                        //HUB.equals(subChildren[0].getName()) &&
                        ARRAY_LENGTH.equals(subChildren[1].getName()) &&
                        ARRAY.equals(subChildren[2].getName())) {
                    displayedVar = new ArrayVar(var, subChildren[1], subChildren[2]);
                } else {
                    // Check for String
                    String type = getSimpleType(var.getType());
                    boolean isString = STRING_TYPES[0].equals(type);
                    boolean likeString = isString;
                    if (!likeString) {
                        for (String strType : STRING_TYPES) {
                            if (strType.equals(type)) {
                                likeString = true;
                                break;
                            }
                        }
                    }
                    if (likeString) {
                        displayedVar = new StringVar(var, type, isString ? null : subChildren);
                    } else {
                        if (children.length == 1 && PUBLIC.equals(children[0].getName())) {
                            // Object children
                            displayedVar = new ObjectVar(var, subChildren);
                        } else {
                            String name = var.getName();
                            if (!name.equals(getNameOrIndex(name))) {
                                displayedVar = new Var(var);
                            } else {
                                displayedVar = var;
                            }
                        }
                    }
                }
            }
            displayedVars.add(displayedVar);
        }
        return displayedVars.toArray(new NIVariable[displayedVars.size()]);
    }

    private static String displayType(String type) {
        if (type.startsWith(COMPRESSED_REF_REFIX)) {
            type = type.substring(COMPRESSED_REF_REFIX.length());
        }
        if (type.endsWith("*")) {
            type = type.substring(0, type.length() - 1).trim();
        }
        return type;
    }

    private static String getSimpleType(String type) {
        type = displayType(type);
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
            if (c != '.' && !Character.isJavaIdentifierPart(c)) {
                return type.substring(0, i);
            }
        }
        return type;
    }

    private static boolean isOfType(String varType, String type) {
        varType = displayType(varType);
        return varType.equals(type) || varType.startsWith(type) && !Character.isJavaIdentifierPart(varType.charAt(type.length()));
    }

    private static int getTypeSize(String type) {
        type = getSimpleType(type);
        switch (type) {
            case "boolean":
            case "byte":
                return 1;
            case "char":
            case "short":
                return 2;
            case "int":
            case "float":
                return 4;
            case "long":
            case "double":
                return 8;
            default:
                return 8;
        }
    }

    private static Map<String, NIVariable> getVarsByName(NIVariable[] vars) {
        switch (vars.length) {
            case 0:
                return Collections.emptyMap();
            case 1:
                return Collections.singletonMap(vars[0].getName(), vars[0]);
            default:
                Map<String, NIVariable> varsByName = new HashMap<>(vars.length);
                for (NIVariable var : vars) {
                    varsByName.put(var.getName(), var);
                }
                return varsByName;
        }
    }

    private static NIVariable[] restrictChildren(NIVariable[] children, int from, int to) {
        if (from > 0 || to < children.length) {
            to = Math.min(to, children.length);
            if (from < to) {
                children = Arrays.copyOfRange(children, from, to);
            } else {
                children = new NIVariable[]{};
            }
        }
        return children;
    }

    private static String getHash(NIVariable[] children) {
        for (NIVariable child : children) {
            if (HASH.equals(child.getName())) {
                String hash = child.getValue();
                try {
                    hash = Integer.toHexString(Integer.parseInt(hash));
                } catch (NumberFormatException ex) {}
                return hash;
            }
        }
        return null;
    }

    private static String getArrayExpression(NIVariable variable) {
        StringBuilder arrayExpression = new StringBuilder(variable.getName());
        while ((variable = variable.getParent()) != null) {
            if (!PUBLIC.equals(variable.getName())) {
                arrayExpression.insert(0, '.');
                arrayExpression.insert(0, variable.getName());
            }
        }
        return arrayExpression.toString();
    }

    private static String getNameOrIndex(String name) {
        if (name.endsWith("]")) {
            int i = name.lastIndexOf(ARRAY+"[");
            if (i > 0) {
                String index = name.substring(i + ARRAY.length() + 1, name.length() - 1);
                return index;
            }
        }
        return name;
    }

    private String readArray(NIVariable lengthVariable, int itemSize) {
        int length = Integer.parseInt(lengthVariable.getValue());
        String expressionPath = lengthVariable.getExpressionPath();
        if (expressionPath != null && !expressionPath.isEmpty()) {
            String addressExpr = "&" + expressionPath;
            return debugger.readMemory(addressExpr, 4, length * itemSize); // length has 4 bytes
        }
        return null;
    }

    private String readArray(NIVariable lengthVariable, int offset, int itemSize) {
        int length = Integer.parseInt(lengthVariable.getValue());
        String expressionPath = lengthVariable.getExpressionPath();
        if (expressionPath != null && !expressionPath.isEmpty()) {
            String addressExpr = "&" + expressionPath;
            return debugger.readMemory(addressExpr, 4 + offset, length * itemSize); // length has 4 bytes
        }
        return null;
    }

    private static NIVariable[] getObjectChildren(NIVariable[] children, int from, int to) {
        for (int i = 0; i < children.length; i++) {
            if (HUB.equals(children[i].getName())) {
                NIVariable[] ch1 = Arrays.copyOf(children, i);
                NIVariable[] ch2 = Arrays.copyOfRange(children, i + 1, children.length);
                if (ch1.length == 0) {
                    return ch2;
                }
                if (ch2.length == 0) {
                    return ch1;
                }
                NIVariable[] ch = new NIVariable[ch1.length + ch2.length];
                System.arraycopy(ch1, 0, ch, 0, ch1.length);
                System.arraycopy(ch2, 0, ch, ch1.length, ch2.length);
                return ch;
            }
        }
        return restrictChildren(children, from, to);
    }

    private class StringVar implements NIVariable {

        private final NIVariable var;
        private final String type;
        private final NIVariable[] children;

        StringVar(NIVariable var, String type, NIVariable[] children) {
            this.var = var;
            this.type = type;
            this.children = children;
        }

        @Override
        public NIFrame getFrame() {
            return var.getFrame();
        }

        @Override
        public String getName() {
            return getNameOrIndex(var.getName());
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getValue() {
            NIVariable pub = getVarsByName(var.getChildren()).get(PUBLIC);
            Map<String, NIVariable> varChildren = getVarsByName(pub.getChildren());
            Map<String, NIVariable> arrayInfo = getVarsByName(varChildren.get(STRING_VALUE).getChildren());
            arrayInfo = getVarsByName(arrayInfo.get(PUBLIC).getChildren());
            NIVariable arrayVariable = arrayInfo.get(ARRAY);
            NIVariable lengthVariable = arrayInfo.get(ARRAY_LENGTH);
            int length = Integer.parseInt(lengthVariable.getValue());
            NIVariable coderVar = varChildren.get(STRING_CODER);
            int coder = -1;
            if (coderVar != null) {
                String coderStr = coderVar.getValue();
                int space = coderStr.indexOf(' ');
                if (space > 0) {
                    coderStr = coderStr.substring(0, space);
                }
                try {
                    coder = Integer.parseInt(coderStr);
                } catch (NumberFormatException ex) {
                }
            }
            String hexArray = readArray(lengthVariable, coder == -1 ? 4 : 0, 2);
            if (hexArray != null) {
                switch (coder) {
                    case 0: // Compressed String on JDK 9+
                        return parseLatin1(hexArray, length);
                    case 1: // UTF-16 String on JDK 9+
                        return parseUTF16(hexArray, length/2);
                    default: // UTF-16 String on JDK 8
                        return parseUTF16(hexArray, length);
                }
            } else { // legacy code
                String arrayExpression = getArrayExpression(arrayVariable);
                char[] characters = new char[length];
                try {
                    for (int i = 0; i < length; i++) {
                        NIVariable charVar = debugger.evaluate(arrayExpression + "[" + i + "]", null, var.getFrame());
                        characters[i] = charVar.getValue().charAt(1);
                    }
                } catch (EvaluateException ex) {
                    return ex.getLocalizedMessage();
                }
                return new String(characters);
            }
        }

        private String parseUTF16(String hexArray, int length) {
            CharsetDecoder cd = Charset.forName("utf-16").newDecoder(); // NOI18N
            ByteBuffer buffer = ByteBuffer.allocate(2);
            char[] characters = new char[length];
            int ih = 0;
            for (int i = 0; i < length; i++) {
                byte b1 = parseByte(hexArray, ih);
                ih += 2;
                byte b0 = parseByte(hexArray, ih);
                ih += 2;
                buffer.rewind();
                buffer.put(b0);
                buffer.put(b1);
                buffer.rewind();
                try {
                    char c = cd.decode(buffer).get();
                    characters[i] = c;
                } catch (CharacterCodingException ex) {
                }
            }
            return new String(characters);
        }

        private String parseLatin1(String hexArray, int length) {
            CharsetDecoder cd = Charset.forName("latin1").newDecoder(); // NOI18N
            ByteBuffer buffer = ByteBuffer.allocate(1);
            char[] characters = new char[length];
            int ih = 0;
            for (int i = 0; i < length; i++) {
                byte b = parseByte(hexArray, ih);
                ih += 2;
                buffer.rewind();
                buffer.put(b);
                buffer.rewind();
                try {
                    char c = cd.decode(buffer).get();
                    characters[i] = c;
                } catch (CharacterCodingException ex) {
                }
            }
            return new String(characters);
        }

        private byte parseByte(String hexArray, int offset) {
            String hex = new String(new char[] {hexArray.charAt(offset), hexArray.charAt(offset + 1)});
            return (byte) (Integer.parseInt(hex, 16) & 0xFF);
        }

        @Override
        public int getNumChildren() {
            return children != null ? children.length : 0;
        }

        @Override
        public NIVariable[] getChildren(int from, int to) {
            return children != null ? getObjectChildren(children, from, to) : new NIVariable[]{};
        }

        @Override
        public NIVariable getParent() {
            return var.getParent();
        }

        @Override
        public String getExpressionPath() {
            return var.getExpressionPath();
        }
    }

    private class ArrayVar implements NIVariable {

        private final NIVariable var;
        private final NIVariable lengthVariable;
        private final int length;
        private final NIVariable array;

        ArrayVar(NIVariable var, NIVariable lengthVariable, NIVariable array) {
            this.var = var;
            this.lengthVariable = lengthVariable;
            int arrayLength;
            try {
                arrayLength = Integer.parseInt(lengthVariable.getValue());
            } catch (NumberFormatException ex) {
                arrayLength = 0;
            }
            this.length = arrayLength;
            this.array = array;
        }

        @Override
        public NIFrame getFrame() {
            return var.getFrame();
        }

        @Override
        public NIVariable getParent() {
            return var.getParent();
        }

        @Override
        public String getName() {
            return getNameOrIndex(var.getName());
        }

        @Override
        public String getType() {
            return displayType(var.getType());
        }

        @Override
        public String getValue() {
            String value = var.getValue();
            if (value.startsWith("@")) {
                value = getType() + value;
            }
            return value + "(length="+length+")";
        }

        @Override
        public int getNumChildren() {
            return length;
        }

        @Override
        public NIVariable[] getChildren(int from, int to) {
            if (from >= 0) {
                to = Math.min(to, length);
            } else {
                from = 0;
                to = length;
            }
            if (from >= to) {
                return new NIVariable[]{};
            }

            String arrayAddress = null;
            String expressionPath = lengthVariable.getExpressionPath();
            if (expressionPath != null && !expressionPath.isEmpty()) {
                String addressExpr = "&" + expressionPath;
                NIVariable addressVariable;
                try {
                    addressVariable = debugger.evaluate(addressExpr, null, lengthVariable.getFrame());
                } catch (EvaluateException ex) {
                    addressVariable = null;
                }
                if (addressVariable != null) {
                    String address = addressVariable.getValue();
                    address = address.toLowerCase();
                    if (address.startsWith("0x")) {
                        arrayAddress = address;
                    }
                }
            }
            NIVariable[] elements = new NIVariable[to - from];
            try {
                if (arrayAddress != null) {
                    String itemExpression = "*(" + getSimpleType(getType()) + "*)(" + arrayAddress + "+";
                    int size = getTypeSize(getType());
                    int offset = 4 + from*size;
                    for (int i = from; i < to; i++) {
                        NIVariable element = debugger.evaluate(itemExpression + offset + ")", Integer.toString(i), var.getFrame());
                        offset += size;
                        elements[i - from] = element;
                    }
                } else {
                    String arrayExpression = getArrayExpression(array);
                    for (int i = from; i < to; i++) {
                        NIVariable element = debugger.evaluate(arrayExpression + "[" + i + "]", Integer.toString(i), var.getFrame());
                        elements[i - from] = element;
                    }
                }
            } catch (EvaluateException ex) {
                return new NIVariable[]{};
            }
            return elements;
        }

        @Override
        public String getExpressionPath() {
            return var.getExpressionPath();
        }
    }

    private class ObjectVar implements NIVariable {

        private final NIVariable var;
        private final NIVariable[] children;

        ObjectVar(NIVariable var, NIVariable[] children) {
            this.var = var;
            this.children = children;
        }

        @Override
        public NIFrame getFrame() {
            return var.getFrame();
        }

        @Override
        public NIVariable getParent() {
            return var.getParent();
        }

        @Override
        public String getName() {
            return getNameOrIndex(var.getName());
        }

        @Override
        public String getType() {
            return displayType(var.getType());
        }

        @Override
        public String getValue() {
            String value = var.getValue();
            if (value.startsWith("@") || value.startsWith("0x")) {
                String hash = getHash(children);
                if (hash == null) {
                    if (value.startsWith("@")) {
                        hash = value.substring(1);
                    } else {
                        hash = value.substring(2);
                    }
                }
                value = getType() + '@' + hash;
            }
            return value;
        }

        @Override
        public int getNumChildren() {
            return children.length;
        }

        @Override
        public NIVariable[] getChildren(int from, int to) {
            return getObjectChildren(children, from, to);
        }

        @Override
        public String getExpressionPath() {
            return var.getExpressionPath();
        }
    }

    private class Var implements NIVariable {

        private final NIVariable var;

        Var(NIVariable var) {
            this.var = var;
        }

        @Override
        public NIFrame getFrame() {
            return var.getFrame();
        }

        @Override
        public NIVariable getParent() {
            return var.getParent();
        }

        @Override
        public String getName() {
            return getNameOrIndex(var.getName());
        }

        @Override
        public String getType() {
            return var.getType();
        }

        @Override
        public String getValue() {
            return var.getValue();
        }

        @Override
        public int getNumChildren() {
            return var.getNumChildren();
        }

        @Override
        public NIVariable[] getChildren(int from, int to) {
            return var.getChildren(from, to);
        }

        @Override
        public NIVariable[] getChildren() {
            return var.getChildren();
        }

        @Override
        public String getExpressionPath() {
            return var.getExpressionPath();
        }
    }
}
