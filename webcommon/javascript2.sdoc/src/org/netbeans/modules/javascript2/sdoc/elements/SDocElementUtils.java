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
package org.netbeans.modules.javascript2.sdoc.elements;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 * Parses Type names, types and their offsets for given strings and
 * creates from them {@code SDocElement}s.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocElementUtils {

    /**
     * Creates element of correct type for given type and remaining element text.
     * @param elementType element type
     * @param elementText tag description text - without the initial type and first spaces, can be empty but never {@code null}
     * @param elementTextStartOffset type description text start offset
     * @return created {@code SDocElement)
     */
    public static SDocElement createElementForType(SDocElementType elementType, String elementText, int elementTextStartOffset) {
        switch (elementType.getCategory()) {
            case DESCRIPTION:
                return SDocDescriptionElement.create(elementType, elementText);
            case IDENT:
                return SDocIdentifierElement.create(elementType, elementText);
            case SIMPLE:
                return SDocSimpleElement.create(elementType);
            case TYPE_SIMPLE:
                TypeInformation simpleInfo = parseTypeInformation(elementType, elementText, elementTextStartOffset);
                return SDocTypeSimpleElement.create(elementType, simpleInfo.getType());
            case TYPE_DESCRIBED:
                TypeInformation descInfo = parseTypeInformation(elementType, elementText, elementTextStartOffset);
                return SDocTypeDescribedElement.create(elementType, descInfo.getType(), descInfo.getDescription());
            case TYPE_NAMED:
                TypeInformation namedInfo = parseTypeInformation(elementType, elementText, elementTextStartOffset);
                return SDocTypeNamedElement.create(elementType, namedInfo.getType(), namedInfo.getDescription(), namedInfo.getName(), namedInfo.isOptional());
            default:
                // unknown sDoc element type
                return SDocDescriptionElement.create(elementType, elementText);
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
        textToParse = removeCurlyBraces(textToParse);
        String[] typesArray = textToParse.split("[,]"); //NOI18N
        for (String string : typesArray) {
            types.add(new TypeUsage(string.trim(), offset + textToParse.indexOf(string.trim())));
        }
        return types;
    }

    private static TypeInformation parseTypeInformation(SDocElementType elementType, String elementText, int descStartOffset) {
        TypeInformation typeInformation = new TypeInformation();
        int process = 0;
        
        String[] parts = elementText.split("[\\s]+"); //NOI18N
        if (parts.length > process) {
            // get type value if any
            if (parts[0].startsWith("{")) { //NOI18N
                int typeOffset = descStartOffset + 1;
                int rparIndex = parts[0].indexOf("}"); //NOI18N
                if (rparIndex == -1) {
                    StringBuilder sb = new StringBuilder();
                    while (process < parts.length) {
                        sb.append(parts[process]);
                        if (parts[process].indexOf("}") == -1) { //NOI18N
                            sb.append(" ");
                            process++;
                        } else {
                            break;
                        }
                    }
                    typeInformation.setType(parseTypes(sb.toString(), typeOffset));
                } else {
                    typeInformation.setType(parseTypes(parts[0], typeOffset));
                }
                process++;
            }

            // get name value (at named types)
            if (parts.length > process && elementType.getCategory() == SDocElementType.Category.TYPE_NAMED) {
                int nameOffset = descStartOffset + elementText.indexOf(parts[process]);
                parseAndStoreTypeDetails(typeInformation, nameOffset, parts[process].trim());
                process++;
            }

            // get description
            StringBuilder sb = new StringBuilder();
            while (process < parts.length) {
                sb.append(parts[process]).append(" "); //NOI18N
                process++;
            }
            typeInformation.setDescription(sb.toString().trim());
        }

        return typeInformation;
    }

    private static void parseAndStoreTypeDetails(TypeInformation typeInfo, int nameOffset, String nameText) {
        boolean optional = nameText.matches("\\[.*\\]"); //NOI18N
        if (optional) {
            nameOffset++;
            nameText = nameText.substring(1, nameText.length() - 1);
        }
        typeInfo.setOptional(optional);
        typeInfo.setName(new Identifier(nameText, nameOffset));
    }

    private static String removeCurlyBraces(String textToParse) {
        return textToParse.replaceAll("[{}]+", "");
    }

    private static class TypeInformation {

        private List<Type> type = Collections.<Type>emptyList();
        private String description = "";
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
