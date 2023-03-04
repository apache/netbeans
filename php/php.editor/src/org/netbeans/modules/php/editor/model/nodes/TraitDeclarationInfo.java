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
package org.netbeans.modules.php.editor.model.nodes;

import java.util.Collection;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TraitDeclarationInfo extends ASTNodeInfo<TraitDeclaration> {

    TraitDeclarationInfo(TraitDeclaration node) {
        super(node);
    }

    public static TraitDeclarationInfo create(TraitDeclaration traitDeclaration) {
        return new TraitDeclarationInfo(traitDeclaration);
    }

    @Override
    public Kind getKind() {
        return Kind.TRAIT;
    }

    @Override
    public String getName() {
        TraitDeclaration traitDeclaration = getOriginalNode();
        return traitDeclaration.getName().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.create(getOriginalNode().getName());
    }

    @Override
    public OffsetRange getRange() {
        TraitDeclaration traitDeclaration = getOriginalNode();
        Identifier name = traitDeclaration.getName();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public Collection<QualifiedName> getUsedTraits() {
        final UsedTraitsVisitor visitor = new UsedTraitsVisitor();
        getOriginalNode().getBody().accept(visitor);
        return visitor.getUsedTraits();
    }
}
