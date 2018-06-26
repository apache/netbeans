/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Petr Pisl
 */
public class PHPDocMethodTag extends PHPDocTypeTag {

    private final List<PHPDocVarTypeTag> params;
    private final PHPDocNode name;

    public PHPDocMethodTag(int start, int end, AnnotationParsedLine kind,
            List<PHPDocTypeNode> returnTypes, PHPDocNode methodName,
            List<PHPDocVarTypeTag> parameters, String documentation) {
        super(start, end, kind, documentation, returnTypes);
        this.params = parameters;
        this.name = methodName;
    }

    public PHPDocNode getMethodName() {
        return name;
    }

    /**
     *
     * @return parameters of the method
     */
    public List<PHPDocVarTypeTag> getParameters() {
        return params;
    }

    @Override
    public String getDocumentation() {
        String retval = documentation;
        if (retval == null) {
            CommentExtractor commentExtractor = CommentExtractorImpl.create(name.getValue());
            retval = commentExtractor.extract(getValue());
        }
        return retval;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    private static class CommentExtractorImpl implements CommentExtractor {

        private final String methodName;
        private String originalDescription;
        private String currentDescription;
        private String comment = "";
        private int bracketBalance = 0;
        private boolean unexpectedCharacter = false;
        private boolean commentMatched = false;

        public static CommentExtractor create(String methodName) {
            return new CommentExtractorImpl(methodName);
        }

        private CommentExtractorImpl(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public String extract(String description) {
            originalDescription = description;
            currentDescription = description;
            int index;
            while ((index = currentDescription.lastIndexOf('(')) > -1) {
                findComment(index);
                if (commentMatched) {
                    break;
                }
            }
            return comment;
        }

        private void findComment(int index) {
            String preDesc = currentDescription.substring(0, index);
            String postDesc = originalDescription.substring(index);
            int matchingBraceIndex = getMatchingBraceIndex(postDesc);
            if (preDesc.trim().endsWith(methodName) && (matchingBraceIndex != 0)) {
                comment = postDesc.substring(matchingBraceIndex + 1).trim();
                commentMatched = true;
            } else {
                currentDescription = preDesc;
            }
        }

        private int getMatchingBraceIndex(String subDescription) {
            int retval = 0;
            for (int i = 0; i < subDescription.length(); i++) {
                countBracketBalance(subDescription.charAt(i));
                if (bracketBalance == 0 && !unexpectedCharacter) {
                    retval = i;
                    break;
                }
                if (unexpectedCharacter) {
                    break;
                }
            }
            return retval;
        }

        private void countBracketBalance(char ch) {
            if (Character.isWhitespace(ch)) {
                return;
            } else if (ch == '(') {
                bracketBalance++;
            } else if (ch == ')') {
                bracketBalance--;
            } else {
                checkUnexpectedCharacter();
            }
        }

        private void checkUnexpectedCharacter() {
            if (bracketBalance == 0) {
                unexpectedCharacter = true;
            }
        }

    }

    private interface CommentExtractor {

        /**
         * Extracts comment part from magic method tag description.
         *
         * @param description Line of magic method tag description.
         * @return Extracted comment part.
         */
        public String extract(String description);

    }
}
