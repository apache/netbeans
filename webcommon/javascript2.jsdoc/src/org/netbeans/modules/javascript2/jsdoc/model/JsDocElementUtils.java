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
package org.netbeans.modules.javascript2.jsdoc.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 * Contains helper classes for work with jsDoc model.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocElementUtils {

    private static final String LOWERCASED_NUMBER = Type.NUMBER.toLowerCase();
    private static final String LOWERCASED_STRING = Type.STRING.toLowerCase();
    private static final String LOWERCASED_BOOLEAN = Type.BOOLEAN.toLowerCase();

    /** Limit for number of spaces inside the type declaration. */
    public static final int LIMIT_SPACES_IN_TYPE = 10;

    /**
     * Creates element of correct type for given type and remaining element text.
     * @param type element type
     * @param tagDescription tag description text - without the initial type and first spaces, can be empty but never {@code null}
     * @param descBeginOffset type description text start offset
     * @return created {@code JsDocElement)
     */
    public static JsDocElement createElementForType(JsDocElementType type, String tagDescription, int descStartOffset) {
        switch (type.getCategory()) {
            case ASSIGN:
                String[] values = tagDescription.split("(\\s)*as(\\s)*"); //NOI18N
                return AssignElement.create(
                        type,
                        (values.length > 0) ? new NamePath(values[0].trim()) : null,
                        (values.length > 1) ? new NamePath(values[1].trim()) : null);
            case DECLARATION:
                return createDeclarationElement(type, tagDescription, descStartOffset);
            case DESCRIPTION:
                return DescriptionElement.create(type, tagDescription);
            case LINK:
                return LinkElement.create(type, new NamePath(tagDescription));
            case NAMED_PARAMETER:
                return createParameterElement(type, tagDescription, descStartOffset);
            case SIMPLE:
                return SimpleElement.create(type);
            case UNNAMED_PARAMETER:
                return createParameterElement(type, tagDescription, descStartOffset);
            default:
                // unknown jsDoc element type
                return DescriptionElement.create(type, tagDescription);
        }
    }

    /**
     * Gets list of {@link Type}s parsed from given string.
     * @param textToParse string to be parsed for types
     * @param offset offset of the textToParse in the file
     * @return list of {@code Type}s
     */
    public static List<Type> parseTypes(String textToParse, int offset) {
        String text = textToParse.trim();
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        if (text.charAt(0) == '(' && text.charAt(text.length() - 1) == ')') {
            text = text.substring(1, text.length() - 1);
            text = text.trim();
        }
        String[] typesArray = text.split("[|]"); //NOI18N
        List<Type> types = new ArrayList<>(typesArray.length);
        for (String string : typesArray) {
            String type = string.trim();
            if (!type.isEmpty()) {
                types.add(createTypeUsage(type, offset + textToParse.indexOf(type)));
            }
        }
        return types;
    }

    private static DeclarationElement createDeclarationElement(JsDocElementType elementType, String elementText, int descStartOffset) {
        String type = elementText;
        int typeOffset = descStartOffset + (!elementText.contains("{") ? 0 : elementText.indexOf("{") + 1); //NOI18N
        if (typeOffset > 0 && elementText.endsWith("}")) { //NOI18N
            type = type.substring(1, type.length() - 1);
            if (type.equals("*")) {
                type = "";
            }
        }
        return DeclarationElement.create(elementType, createTypeUsage(type, typeOffset));
    }

    protected static TypeUsage createTypeUsage(String type, int offset) {
        // see issue #233176
        if (LOWERCASED_STRING.equals(type)) {
            return new TypeUsage(Type.STRING, offset);
        } else if (LOWERCASED_NUMBER.equals(type)) {
            return new TypeUsage(Type.NUMBER, offset);
        } else if (LOWERCASED_BOOLEAN.equals(type)) {
            return new TypeUsage(Type.BOOLEAN, offset);
        } else {
            String correctedType = type;
            if (correctedType.indexOf('~') > 0) {
                // we dont't replace tilda if it's on the first position
                correctedType = correctedType.replace('~', '.'); // replacing tilda with dot. See issue #25110
            }
            return new TypeUsage(correctedType, offset);
        }
    }

    private static ParameterElement createParameterElement(JsDocElementType elementType,
            String elementText, int descStartOffset) {
        int typeOffset = -1, nameOffset = -1;
        String types = "", desc = ""; //NOI18N
        StringBuilder name = new StringBuilder();
        int process = 0;
        String[] parts = elementText.split("[\\s]+"); //NOI18N

        if (parts.length > process) {
            //extract type info, handle {} inside of type
            //e.g. {{a: number, b: string}} myObj
            int curlyStart = elementText.indexOf("{");
            int curlyEnd = -1;
            if (curlyStart != -1) {
                typeOffset = descStartOffset + curlyStart + 1;
                int openCurlyBracesForType = 0;
                char[] cArray = elementText.toCharArray();
                for (int i = 0; i < cArray.length; i++) {
                    if (cArray[i] == '{') {
                        openCurlyBracesForType++;
                    } else if (cArray[i] == '}') {
                        openCurlyBracesForType--;
                        if (openCurlyBracesForType == 0) {
                            curlyEnd = i;
                            break;
                        }
                    }
                }
                if (curlyEnd != -1) {
                    String typeInfo = elementText.substring(curlyStart + 1, curlyEnd);//within curly braces
                    types = typeInfo.trim();
                } else {
                    //if type at index=0 extract first part as type
                    if (curlyStart == 0) {
                        types = parts[0].trim();
                        process++;
                    } else {
                        //else return the part containing type
                        for (String part : parts) {
                            if (part.startsWith("{")) {
                                curlyEnd = curlyStart + part.length();
                                types = part;
                                break;
                            }
                        }
                    }
                }
                if (types.trim().equals("*")) {
                    types = "";
                }
            }

            //if type at index=0, extract name and desc from the remaining text
            if ((curlyStart == 0) && (curlyEnd != -1)) {
                parts = elementText.substring(Math.min(curlyEnd + 1, elementText.length())).trim().split("[\\s]+");
            } else if (curlyStart > 0) {
                //use entire text minus the types part to get name and desc
                String typesStr = elementText.substring(curlyStart, Math.min(curlyEnd + 1, elementText.length()));
                StringBuilder buf = new StringBuilder(elementText);
                elementText = buf.replace(curlyStart, curlyStart + typesStr.length(), "").toString();
                parts = elementText.split("[\\s]+");
            }

            // get name value (mandatory part)
            if (parts.length > process && elementType.getCategory() == JsDocElement.Category.NAMED_PARAMETER) {
                nameOffset = descStartOffset + elementText.indexOf(parts[process], types.length());
                String currentPart = parts[process].trim();
                if (!currentPart.isEmpty() && currentPart.charAt(0) == '[') {
                    // has default value
                    int start = elementText.indexOf('[', types.length());
                    if (start > 0) {
                        int end = elementText.indexOf(']', start);
                        if (end > 0) {
                            name.append(elementText.substring(start, end + 1));
                        } else {
                            name.append(elementText.substring(start)).append(']');// close the default value
                        }
                    }
                    while (process < parts.length - 1 && currentPart.charAt(currentPart.length() - 1) != ']') {
                         process++;
                         currentPart = parts[process].trim();
                    }

                    if (process < parts.length && currentPart.charAt(currentPart.length() - 1) == ']') {
                        process++;
                    }
                } else {
                    name.append(parts[process].trim());
                    process++;
                    if (name.toString().contains("\"") || name.toString().contains("'")) { //NOI18N
                        process = buildNameForString(name, process, parts);
                    }
                }
            }

            // get description
            StringBuilder sb = new StringBuilder();
            while (process < parts.length) {
                sb.append(parts[process]).append(" "); //NOI18N
                process++;
            }
            desc = sb.toString().trim();
        }

        if (elementType.getCategory() == JsDocElement.Category.NAMED_PARAMETER) {
            return NamedParameterElement.createWithDiagnostics(elementType,
                    new Identifier(name.toString(), nameOffset), parseTypes(types, typeOffset), desc);
        } else {
            return UnnamedParameterElement.create(elementType, parseTypes(types, typeOffset), desc);
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private static int buildNameForString(StringBuilder name, int currentOffset, String[] parts) {
        // TODO - better would be to solve that using lexer
        String nameString = name.toString();
        if ((nameString.contains("\"") && (nameString.indexOf("\"") == nameString.lastIndexOf("\""))) //NOI18N
                || (nameString.contains("'") && nameString.indexOf("'") == nameString.lastIndexOf("'"))) { //NOI18N
            // string with spaces
            boolean endOfString = false;
            while (currentOffset < parts.length && !endOfString) {
                name.append(" ").append(parts[currentOffset]); //NOI18N
                if (parts[currentOffset].contains("\"") || parts[currentOffset].contains("'")) { //NOI18H
                    endOfString = true;
                }
                currentOffset++;
            }
        }
        return currentOffset;
    }

    public static class GoogleCompilerSytax {
        public static boolean canBeThisSyntax(String type) {
            boolean result = (type.charAt(type.length() - 1) == '=');
            return result;
        }

        public static boolean isMarkedAsOptional(String type) {
            boolean result = (type.charAt(type.length() - 1) == '=');
            return result;
        }

        public static String removeSyntax(String type) {
            String result = type;
            if (isMarkedAsOptional(type)) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        }
    }

}
