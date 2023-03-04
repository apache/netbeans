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
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 * Represents a PHP comment
 * <pre>e.g.<pre> // this is a single line comment
 * # this is a single line comment
 * /** this is php doc block (end php docblock here)
 */
public class Comment extends ASTNode {

    public enum Type {

        TYPE_SINGLE_LINE ("singleLine"),    //NOI18N
        TYPE_MULTILINE ("multiLine"),       //NOI18N
        TYPE_PHPDOC ("phpDoc"),             //NOI18N
        TYPE_VARTYPE("vartype");            //NOI18N

        private final String text;

        Type(String textRepresentation) {
            text = textRepresentation;
        }

        public String toString() {
            return text;
        }
    };

    private Type commentType;

    public Comment(int start, int end, Type type) {
        super(start, end);
        commentType = type;
    }

    public Type getCommentType() {
        return commentType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
