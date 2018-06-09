/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
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
            String[] parts = definition.split("[ \t]+"); //NOI18N
            if (isExpectedPartsLength(isPHPDoc, parts)
                    && parts[variableIndex].charAt(0) == '$') { //NOI18N
                //counting types
                String[] types = parts[typeIndex].split("[|]"); //NOI18N
                int typePosition = startOffset + comment.indexOf(parts[typeIndex]);
                ArrayList<PHPDocTypeNode> typeNodes = new ArrayList<>();
                for (String type: types) {
                    startDocNode = typePosition + parts[typeIndex].indexOf(type);
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
                        String constantName = type.substring(index + 2, type.length());
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
