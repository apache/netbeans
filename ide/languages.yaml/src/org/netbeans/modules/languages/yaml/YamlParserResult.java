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
package org.netbeans.modules.languages.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.snakeyaml.engine.v2.nodes.Node;

/**
 * A result from Parsing YAML
 *
 * @author Tor Norbye
 */
public class YamlParserResult extends ParserResult {

    private final List<Error> errors = new ArrayList<Error>();
    private List<Node> nodes;
    private List<? extends StructureItem> items;

    public YamlParserResult(List<Node> nodes, YamlParser parser, Snapshot snapshot, boolean valid) {
        super(snapshot);
        assert nodes != null;
        this.nodes = nodes;
    }

    public List<Node> getRootNodes() {
        return nodes;
    }

    public void addError(Error error) {
        errors.add(error);
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    protected void invalidate() {
        // FIXME parsing API
        // remove from parser cache (?)
    }

    public synchronized List<? extends StructureItem> getItems() {
        if (items == null) {
            items = new YamlScanner().scanStructure(this);
        }

        return items;
    }

    public void setItems(List<? extends StructureItem> items) {
        this.items = items;
    }

    public static OffsetRange getAstRange(Node node) {
        int s = node.getStartMark().get().getPointer();
        int e = node.getEndMark().get().getPointer();
        return new OffsetRange(s, e);
    }
}
