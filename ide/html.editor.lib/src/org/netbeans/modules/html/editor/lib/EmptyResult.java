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

package org.netbeans.modules.html.editor.lib;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.ParseResult;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.Node;

/**
 *
 * @author marekfukala
 */
public class EmptyResult implements ParseResult {

    private HtmlSource source;

    public EmptyResult(HtmlSource source) {
        this.source = source;
    }

    @Override
    public HtmlSource source() {
        return source;
    }

    @Override
    public Node root() {
        return new RootNode(source.getSourceCode());
    }

    @Override
    public Collection<ProblemDescription> getProblems() {
        return Collections.emptyList();
    }

}
