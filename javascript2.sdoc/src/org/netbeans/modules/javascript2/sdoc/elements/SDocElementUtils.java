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
