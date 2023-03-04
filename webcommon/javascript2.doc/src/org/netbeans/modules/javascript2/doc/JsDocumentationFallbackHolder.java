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
package org.netbeans.modules.javascript2.doc;

import com.oracle.js.parser.ir.Node;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Can be returned by the {@link JsDocumentationFallbackProvider}. It provides
 * empty data. It is used to prevent null checking in the model, visitor codes.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class JsDocumentationFallbackHolder extends JsDocumentationHolder {

    public JsDocumentationFallbackHolder(JsDocumentationProvider provider, Snapshot snapshot) {
        super(provider, snapshot);
    }

    @Override
    public List getReturnType(Node node) {
        return Collections.emptyList();
    }

    @Override
    public List getParameters(Node node) {
        return Collections.emptyList();
    }

    @Override
    public Documentation getDocumentation(Node node) {
        return null;
    }

    @Override
    public boolean isDeprecated(Node node) {
        return false;
    }

    @Override
    public Set getModifiers(Node node) {
        return Collections.emptySet();
    }

    @Override
    public Map getCommentBlocks() {
        return Collections.emptyMap();
    }

}
