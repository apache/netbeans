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

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;

import static org.snakeyaml.engine.v2.nodes.NodeType.*;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

/**
 * Semantic Analyzer for YAML
 *
 * @author Tor Norbye
 */
public class YamlSemanticAnalyzer extends SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        YamlParserResult ypr = (YamlParserResult) result;
        if (ypr == null || ypr.getRootNodes().isEmpty()) {
            this.semanticHighlights = Collections.emptyMap();
            return;
        }

        List<Node> rootNodes = ypr.getRootNodes();

        Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<OffsetRange, Set<ColoringAttributes>>(100);

        IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<Object, Boolean>(100);
        for (Node root : rootNodes) {
            addHighlights(ypr, root, highlights, seen, 0);
        }

        this.semanticHighlights = highlights;
    }

    private void addHighlights(YamlParserResult ypr, Node node, Map<OffsetRange, Set<ColoringAttributes>> highlights, IdentityHashMap<Object, Boolean> seen, int depth) {
        if (depth > 10 || node == null) {
            // Avoid boundless recursion; some datastructures from YAML appear to be recursive
            return;
        }
        if (seen.containsKey(node)) {
            return;
        }
        seen.put(node, Boolean.TRUE);

        switch (node.getNodeType()) {
            case MAPPING:
                MappingNode mappings = (MappingNode) node;
                List<NodeTuple> tuples = mappings.getValue();
                for (NodeTuple tuple : tuples) {
                    addHighlights(ypr, tuple.getValueNode(), highlights, seen, depth + 1);
                }
                break;
            case SEQUENCE:
                SequenceNode sequence = (SequenceNode) node;
                List<Node> nodes = sequence.getValue();
                for (Node node1 : nodes) {
                    addHighlights(ypr, node, highlights, seen, depth + 1);
                }
                break;
            case SCALAR:
                ScalarNode scalar = (ScalarNode) node;
                OffsetRange range = YamlParserResult.getAstRange(scalar);
                highlights.put(range, ColoringAttributes.METHOD_SET);
                break;
        }
    }
}
