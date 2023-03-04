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
package org.netbeans.modules.html.editor.lib.plain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;

/**
 *
 * @author marekfukala
 */
public class ProblematicOpenTagElement extends LongOpenTagElement {

    private ProblemDescription problem;

    public ProblematicOpenTagElement(CharSequence document, int from, int length,
            byte nameLen,
            List<Attribute> attribs,
            boolean isEmpty,
            ProblemDescription problem) {
        super(document, from, length, nameLen, attribs, isEmpty);
        this.problem = problem;
    }

    @Override
    public Collection<ProblemDescription> problems() {
        return Collections.singleton(problem);
    }

}
