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
 * Represents a type in the tags like @param, @return, @throws etc.
 * There can be more types associated with one return value or param.
 *
 * @author Petr Pisl
 */
public class PHPDocTypeTag extends PHPDocTag {

    private final List<PHPDocTypeNode> types;
    protected String documentation;

    public PHPDocTypeTag(int start, int end, AnnotationParsedLine kind, String value, List<PHPDocTypeNode> types) {
        super(start, end, kind, value);
        this.types = types;
        this.documentation = null;
    }

    /**
     *
     * @return list of PHPDocNode or PHPDocStaticAccessType
     */
    public List<PHPDocTypeNode> getTypes() {
        return types;
    }

    @Override
    public String getDocumentation() {
        if (documentation == null && !types.isEmpty()) {
            PHPDocTypeNode lastType = types.get(0);
            for (PHPDocTypeNode node : types) {
                if (lastType.getEndOffset() < node.getEndOffset()) {
                    lastType = node;
                }
            }
            // e.g. (A&B&C)|(A&B) description
            // arr[] description
            // The last type is `B`. If we get the position using String.indexOf(), we get the wrong position
            String[] split = CodeUtils.WHITE_SPACES_PATTERN.split(getValue().trim(), 2);
            String value = lastType.getValue();
            if (split[0]. contains(value)) {
                value = split[0];
            }
            int indexAfterType = getValue().indexOf(value) + value.length();
            if (indexAfterType < 0) {
                documentation = ""; // NOI18N
            } else {
                documentation = getValue().substring(indexAfterType).trim();
            }
        }
        return documentation;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }


}
