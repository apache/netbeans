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

import java.util.Collections;
import java.util.List;

/**
 * Represents a trait declaration.
 *
 * <pre>e.g.
 * trait MyTrait { },
 * #[A(1)]
 * trait MyTrait { } // [NETBEANS-4443] PHP 8.0
 * </pre>
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TraitDeclaration extends TypeDeclaration {

    public TraitDeclaration(int start, int end, final Identifier name, final Block body) {
        this(start, end, name, body, Collections.emptyList());
    }

    private TraitDeclaration(int start, int end, final Identifier name, final Block body, List<Attribute> attributes) {
        super(start, end, name, null, body, attributes);
    }

    public static TraitDeclaration create(TraitDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new TraitDeclaration(
                start,
                declaration.getEndOffset(),
                declaration.getName(),
                declaration.getBody(),
                attributes
        );
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
