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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.List;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Petr Pisl
 */
public class PHPDocMethodTag extends PHPDocTypeTag {

    private final List<PHPDocVarTypeTag> params;
    private final PHPDocNode name;
    private final boolean isStatic;
    private final String returnType;

    public PHPDocMethodTag(int start, int end, AnnotationParsedLine kind,
            List<PHPDocTypeNode> returnTypes, PHPDocNode methodName,
            List<PHPDocVarTypeTag> parameters, String documentation) {
        this(start, end, kind, returnTypes, methodName, parameters, documentation, false);
    }

    /**
     * Constructor.
     *
     * @param start start offset
     * @param end end offset
     * @param kind parsed annotation line
     * @param returnTypes return types
     * @param methodName method name
     * @param parameters parameters
     * @param documentation documentation
     * @param isStatic static flag
     * @since 1.84.0
     */
    public PHPDocMethodTag(int start, int end, AnnotationParsedLine kind,
            List<PHPDocTypeNode> returnTypes, PHPDocNode methodName,
            List<PHPDocVarTypeTag> parameters, String documentation, boolean isStatic) {
        super(start, end, kind, documentation, returnTypes);
        this.params = parameters;
        this.name = methodName;
        this.isStatic = isStatic;
        this.returnType = getReturnType(documentation);
    }

    private String getReturnType(String documentation) {
        String type = documentation.trim();
        String[] split = CodeUtils.WHITE_SPACES_PATTERN.split(type, 2);
        if (split[0].equals(org.netbeans.modules.php.editor.model.impl.Type.STATIC)) {
            type = split[1];
        }
        if (type.startsWith(name.getValue() + "(")) { // NOI18N
            return ""; // NOI18N
        }
        split = CodeUtils.WHITE_SPACES_PATTERN.split(type, 2);
        return split[0];
    }

    /**
     * Check whethere the method is static.
     *
     * @return {@code true} if the method is static, {@code false} otherwise
     * @since 1.84.0
     */
    public boolean isStatic() {
        return isStatic;
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

    public String getReturnType() {
        return returnType;
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
