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

package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;

/**
 * PHPVarComment pattern: The first one is the IDE's own pattern. See
 * {@link https://github.com/php-fig/fig-standards/blob/master/proposed/phpdoc.md}
 * about the second one.
 * <pre>
 * &#47;*  @var $variableName TypeName *&#47;
 * &#47;** @var TypeName $variableName Description *&#47;
 * </pre>
 *
 * @author Petr Pisl
 */
public class PHPVarCommentParser {

    private static final String PHPDOCTAG = "@" + PHPDocTag.Type.VAR.name().toLowerCase(); //NOI18N

    PHPVarComment parse(final int startOffset, final int endOffset, final String comment) {
        boolean isPHPDoc = comment.startsWith("/**"); // NOI18N
        int variableIndex = isPHPDoc ? 2 : 1;
        int typeIndex = isPHPDoc ? 1 : 2;

        int index = comment.indexOf(PHPDOCTAG);
        if (index > -1) {
            String definition = comment.substring(index);
            index = definition.indexOf("*/"); //NOI18N
            if (index > -1) {
                definition = definition.substring(0, index);
            }
            int startDocNode;
            int endPosition = 0;
            String[] parts = CodeUtils.WHITE_SPACES_PATTERN.split(definition);
            if (isExpectedPartsLength(isPHPDoc, parts)
                    && parts[variableIndex].charAt(0) == '$') { //NOI18N
                //counting types
                String typePart = parts[typeIndex];
                String[] types = Type.splitTypes(typePart);
                int typePosition = startOffset + comment.indexOf(typePart);
                ArrayList<PHPDocTypeNode> typeNodes = new ArrayList<>();
                int fromEndIndexOfLastType = 0;
                for (String type: types) {
                    int indexOfType = typePart.indexOf(type, fromEndIndexOfLastType);
                    startDocNode = typePosition + indexOfType;
                    fromEndIndexOfLastType = indexOfType + type.length();
                    index = type.indexOf("::"); //NOI18N
                    boolean isArray = (type.indexOf('[') > 0  && type.indexOf(']') > 0);
                    if (isArray) {
                        type = type.substring(0, type.indexOf('[')).trim();
                    }
                    PHPDocTypeNode docType;
                    endPosition = startDocNode + type.length();
                    if (index == -1) {
                        docType = new PHPDocTypeNode(startDocNode, endPosition, type, isArray);
                    } else {
                        String className = type.substring(0, index);
                        String constantName = type.substring(index + 2);
                        PHPDocNode classNameNode = new PHPDocNode(startDocNode, startDocNode + className.length(), className);
                        PHPDocNode constantNode = new PHPDocNode(startDocNode + className.length() + 2, startDocNode + type.length(), constantName);
                        docType = new PHPDocStaticAccessType(startDocNode, startDocNode + type.length(), type, classNameNode, constantNode);
                    }
                    typeNodes.add(docType);
                }
                // counting variable
                String variableName = parts[variableIndex];
                int indexOfArrayDimension = parts[variableIndex].indexOf("["); //NOI18N
                if (indexOfArrayDimension != -1) {
                    variableName = parts[variableIndex].substring(0, indexOfArrayDimension);
                }
                startDocNode = startOffset + comment.indexOf(variableName);
                PHPDocNode variableNode = new PHPDocNode(startDocNode, startDocNode + variableName.length(), variableName);
                startDocNode = startOffset + comment.indexOf(PHPDOCTAG);
                PHPDocVarTypeTag variableType =  new PHPDocVarTypeTag(startDocNode, endPosition, PHPDocTag.Type.VAR, definition, typeNodes, variableNode);
                return new PHPVarComment(startOffset, endOffset, variableType);
            }
        }
        return null;
    }

    private boolean isExpectedPartsLength(boolean isPHPDoc, String[] parts) {
        if (isPHPDoc) {
            return parts.length >= 3;
        }
        return parts.length == 3;
    }
}
