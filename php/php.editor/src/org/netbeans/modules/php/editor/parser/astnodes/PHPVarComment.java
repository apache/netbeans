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
 * Represents a single line comment, where user is able to assigna a type to variable
 * <pre> @var $variable type </pre> . It has to be single line comment. Also it can
 * contains mixed type. e.g. <pre> @var $variable type1|type2 </pre>
 *
 * <b>NOTE:</b> There is an order difference.
 * <pre>
 * &#47;*  @var $variableName TypeName *&#47;
 * &#47;** @var TypeName $variableName Description *&#47;
 * </pre>
 * @author Petr Pisl
 */
public class PHPVarComment extends Comment {

    private final PHPDocVarTypeTag variable;

    public PHPVarComment(int start, int end, PHPDocVarTypeTag  variable) {
        super(start, end, Comment.Type.TYPE_VARTYPE);
        this.variable = variable;
    }


    public PHPDocVarTypeTag getVariable() {
        return variable;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
