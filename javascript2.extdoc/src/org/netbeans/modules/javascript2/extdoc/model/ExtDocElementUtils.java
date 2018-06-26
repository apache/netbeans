/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
