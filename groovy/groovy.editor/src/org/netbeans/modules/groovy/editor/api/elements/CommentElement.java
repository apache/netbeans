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

package org.netbeans.modules.groovy.editor.api.elements;

import org.netbeans.modules.csl.api.ElementKind;

/**
 * Element describing a Groovy comment
 *
 * @author Tor Norbye
 * @author Gopala Krishnan S
 */


public class CommentElement extends GroovyElement {

    private final String text;

    public CommentElement(String text) {
        super();
        this.text = text;
    }

    @Override
    public String getName() {
        return text;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.KEYWORD;
    }
}
