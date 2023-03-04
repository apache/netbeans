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
package org.netbeans.modules.javascript2.extdoc.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 * Parses Type names, types and their offsets for given strings and
 * creates from them {@code ExtDocElement}s.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocElementUtils {

    /**
     * Creates element of correct type for given type and remaining element text.
     * @param elementType element type
     * @param elementText tag description text - without the initial type and first spaces, can be empty but never {@code null}
     * @param elementTextStartOffset type description text start offset
     * @return created {@code ExtDocElement)
     */
    public static ExtDocElement createElementForType(ExtDocElementType elementType, String elementText, int elementTextStartOffset) {
        switch (elementType.getCategory()) {
            case DESCRIPTION:
                return ExtDocDescriptionElement.create(elementType, elementText);
            case IDENT_SIMPLE:
                return ExtDocIdentSimpleElement.create(elementType, elementText);
            case IDENT_DESCRIBED:
                String[] identAttributes = parseIdentAttributes(elementText);
                return ExtDocIdentDescribedElement.create(elementType, identAttributes[0], identAttributes[1]);
            case SIMPLE:
                return ExtDocSimpleElement.create(elementType);
            case TYPE_SIMPLE:
                TypeInformation simpleInfo = parseTypeInformation(elementType, elementText, elementTextStartOffset);
                return ExtDocTypeSimpleElement.create(elementType, simpleInfo.getType());
            case TYPE_DESCRIBED:
                TypeInformation descInfo = parseTypeInformation(elementType, elementText, elementTextStartOffset);
                return ExtDocTypeDescribedElement.create(elementType, descInfo.getType(), descInfo.getDescription());
            case TYPE_NAMED:
                TypeInformation namedInfo = parseTypeInformation(elementType, elementText, elementTextStartOffset);
                return ExtDocTypeNamedElement.create(elementType, namedInfo.getType(), namedInfo.getDescription(), namedInfo.getName(), namedInfo.isOptional(), namedInfo.getDefaultValue());
            default:
                // unknown extDoc element type
                return ExtDocDescriptionElement.create(elementType, elementText);
        }
    }
    
    public static String[] parseIdentAttributes(String elementText) {
        String[] parts = elementText.split("[\\s]+"); //NOI18N
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        } else {
            return new String[]{parts[0], elementText.substring(parts[0].length()).trim()};
        }
    }

    /**
     * Gets list of {@link Type}s parsed from given string.
     * @param textToParse string to be parsed for types
     * @param offset offset of the textToParse in the file
     * @return list of {@code Type}s
     */
    public static List<Type> parseTypes(String textToParse, int offset) {
        List<Type> types = new LinkedList<Type>();
        String[] typesArray = textToParse.split("[/]"); //NOI18N
        for (String string : typesArray) {
            types.add(new TypeUsage(string, offset + textToParse.indexOf(string)));
        }
        return types;
    }

    private static TypeInformation parseTypeInformation(ExtDocElementType elementType, String elementText, int descStartOffset) {
        TypeInformation typeInformation = new TypeInformation();
        int process = 0;

        String[] parts = elementText.split("[\\s]+"); //NOI18N
        if (parts.length > process) {
            // get type value if any
            if (parts[0].startsWith("{")) { //NOI18N
                int typeOffset = descStartOffset + 1;
                int rparIndex = parts[0].indexOf("}"); //NOI18N
                if (rparIndex == -1) {
                    typeInformation.setType(parseTypes(parts[0].trim(), typeOffset));
                } else {
                    typeInformation.setType(parseTypes(parts[0].substring(1, rparIndex), typeOffset));
                }
                process++;
            }

            // get name value (at named types)
            if (parts.length > process && elementType.getCategory() == ExtDocElementType.Category.TYPE_NAMED) {
                StringBuilder name = new StringBuilder();
                int nameOffset = descStartOffset + elementText.indexOf(parts[process]);
                name.append(parts[process].trim());
                process++;
                if (name.toString().contains("\"") || name.toString().contains("'")) { //NOI18N
                    process = getOptionalParamNameWithString(name, process, parts);
                }
                parseAndStoreTypeDetails(typeInformation, nameOffset, name.toString().trim());
            }

            // get description
            StringBuilder description = new StringBuilder();
            while (process < parts.length) {
                description.append(parts[process]).append(" "); //NOI18N
                process++;
            }
            typeInformation.setDescription(description.toString().trim());
        }

        return typeInformation;
    }

    private static int getOptionalParamNameWithString(StringBuilder name, int currentOffset, String[] parts) {
        // TODO - better would be to solve that using lexer
        String nameString = name.toString();
        if ((nameString.indexOf("\"") != -1 && (nameString.indexOf("\"") == nameString.lastIndexOf("\""))) //NOI18N
                || (nameString.indexOf("'") != -1 && nameString.indexOf("'") == nameString.lastIndexOf("'"))) { //NOI18N
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

    private static void parseAndStoreTypeDetails(TypeInformation typeInfo, int nameOffset, String nameText) {
        boolean optional = nameText.matches("\\[.*\\]"); //NOI18N
        if (optional) {
            nameOffset++;
            nameText = nameText.substring(1, nameText.length() - 1);
            int indexOfEquals = nameText.indexOf("=");
            if (indexOfEquals != -1) {
                typeInfo.setDefaultValue(nameText.substring(indexOfEquals + 1));
                nameText = nameText.substring(0, indexOfEquals);
            }
        }
        typeInfo.setOptional(optional);
        typeInfo.setName(new Identifier(nameText, nameOffset));
    }

    private static class TypeInformation {

        private List<Type> type = Collections.<Type>emptyList();
        private String description = "";
        private String defaultValue = null;
        private Identifier name = null;
        private boolean optional = false;

        public void setName(Identifier name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setType(List<Type> type) {
            this.type = type;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Identifier getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<Type> getType() {
            return type;
        }

        public boolean isOptional() {
            return optional;
        }

    }

}
