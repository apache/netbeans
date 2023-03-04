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
package org.netbeans.modules.languages.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * A result from Parsing YAML
 *
 * @author Tor Norbye
 */
public class YamlParserResult extends ParserResult {

    private final List<Error> errors = new ArrayList<>();
    private final List<StructureItem> structure = new ArrayList<>();

    public YamlParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    public void addError(Error error) {
        boolean alreadyReported = false;
        for (Error e : errors) {
            if ((e.getStartPosition() <= error.getStartPosition()) && (e.getEndPosition() >= error.getStartPosition())) {
                alreadyReported = true;
                break;
            }
        }
        if (!alreadyReported) {
            errors.add(error);
        }
    }

    public void addStructure(List<? extends StructureItem> items) {
        structure.addAll(items);
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
        return structure;
    }

}
