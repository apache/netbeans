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
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TraitMethodAliasDeclaration extends Statement {
    private final Identifier oldMethodName;
    private final Identifier newMethodName;
    private final Expression traitName;
    private final Modifier modifier;

    public enum Modifier {
        PUBLIC,
        PROTECTED,
        PRIVATE
    }

    public TraitMethodAliasDeclaration(final int start, final int end, final Identifier oldMethodName, final Identifier newMethodName, final Expression traitName, final Modifier modifier) {
        super(start, end);
        this.oldMethodName = oldMethodName;
        this.newMethodName = newMethodName;
        this.traitName = traitName;
        this.modifier = modifier;
    }

    public Identifier getNewMethodName() {
        return newMethodName;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public Expression getTraitName() {
        return traitName;
    }

    public Identifier getOldMethodName() {
        return oldMethodName;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getTraitName() + "::" + getOldMethodName() + " as " + getModifier() + " " + getNewMethodName(); //NOI8N
    }

}
