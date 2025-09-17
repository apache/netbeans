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

package org.netbeans.modules.web.el;

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.el.options.MarkOccurencesSettings;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 * Occurrences finder for Expression Language.
 *
 * @author Erno Mononen
 */
final class ELOccurrencesFinder extends OccurrencesFinder<ELParserResult> {

    private int caretPosition;
    private boolean cancelled;
    private final Map<OffsetRange, ColoringAttributes> occurrences = new HashMap<>();

    public ELOccurrencesFinder() {
    }

    @Override
    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return occurrences;
    }

    @Override
    public void run(ELParserResult result, SchedulerEvent event) {
        occurrences.clear();
        if (checkAndResetCancel()) {
            return;
        }
        computeOccurrences(result);
    }

    @Override
    public int getPriority() {
        return 200; // not sure what to return here, 200 this is just a random number.
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean isKeepMarks() {
        return MarkOccurencesSettings
                .getCurrentNode()
                .getBoolean(MarkOccurencesSettings.KEEP_MARKS, true);
    }

    @Override
    public boolean isMarkOccurrencesEnabled() {
        return MarkOccurencesSettings
                .getCurrentNode()
                .getBoolean(MarkOccurencesSettings.ON_OFF, true);
    }

    private void computeOccurrences(final ELParserResult parserResult) {
        ELElement current = parserResult.getElementAt(caretPosition);
        if (current == null) {
            return;
        }
        final Node targetNode = current.findNodeAt(caretPosition);
        if (targetNode == null || targetNode.getImage() == null) {
           return;
        }
        final Pair<ELElement,Node> target = Pair.of(current, targetNode);
        // find other similar nodes
        final List<Pair<ELElement, Node>> matching = new ArrayList<>();
        for (final ELElement eLElement : parserResult.getElements()) {
            if (checkAndResetCancel()) {
                return;
            }
            if (!eLElement.isValid()) {
                continue;
            }
            eLElement.getNode().accept((Node node) -> {
                if (node.getClass().equals(targetNode.getClass())
                        && targetNode.getImage().equals(node.getImage())) {
                    matching.add(Pair.of(eLElement, node));
                }
            });
        }
        final FileObject file = parserResult.getFileObject();
        JavaSource jsource = JavaSource.create(ELTypeUtilities.getElimplExtendedCPI(file));
        try {
            jsource.runUserActionTask((CompilationController info) -> {
                info.toPhase(JavaSource.Phase.RESOLVED);
                occurrences.putAll(findMatchingTypes(CompilationContext.create(file, info), target, matching));
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (this.occurrences.isEmpty()) {
            // perhaps the caret is on a resource bundle key node
            occurrences.putAll(findMatchingResourceBundleKeys(target, parserResult));
        }
    }

    private Map<OffsetRange, ColoringAttributes> findMatchingResourceBundleKeys(Pair<ELElement, Node> target, ELParserResult parserResult) {
        ResourceBundles resourceBundles = ResourceBundles.get(parserResult.getFileObject());
        if (!resourceBundles.canHaveBundles()) {
            return Collections.emptyMap();
        }
        List<Pair<AstIdentifier, Node>> keys = new ArrayList<>();
        // the logic here is a bit strange, maybe should add new methods to ResourceBundles
        // for a more straightforward computation.
        // first, check whether the current EL elements has keys
        keys.addAll(resourceBundles.collectKeys(target.first().getNode()));
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        // second, if yes, check whether it has a key matching to the node under the caret
        boolean found = false;
        for (Pair<AstIdentifier, Node> pair : keys) {
            if (pair.second().equals(target.second())) {
                found = true;
                break;
            }
        }
        if (!found) {
            return Collections.emptyMap();
        }
        // third: collect the other matching keys and return them
        Map<OffsetRange, ColoringAttributes> result = new HashMap<>();
        for (ELElement each : parserResult.getElements()) {
            if (!each.isValid()) {
                continue;
            }
            for (Pair<AstIdentifier, Node> candidate : resourceBundles.collectKeys(each.getNode())) {
                if (candidate.second().equals(target.second())) {
                    OffsetRange range = each.getOriginalOffset(candidate.second());
                    result.put(range, ColoringAttributes.MARK_OCCURRENCES);
                }
            }
        }
        return result;
    }

    private Map<OffsetRange, ColoringAttributes> findMatchingTypes(CompilationContext info, Pair<ELElement,Node> target, List<Pair<ELElement,Node>> candidates) {
        Element targetType = ELTypeUtilities.resolveElement(info, target.first(), target.second());
        Map<OffsetRange, ColoringAttributes>  result = new HashMap<>();

        for (Pair<ELElement,Node> candidate : candidates) {
            if (checkAndResetCancel()) {
                return result;
            }
            Element type = ELTypeUtilities.resolveElement(info, candidate.first(), candidate.second());
            if (type != null && type.equals(targetType)) {
                OffsetRange range = candidate.first().getOriginalOffset(candidate.second());
                result.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }
        return result;
    }

    private boolean checkAndResetCancel() {
        if (cancelled) {
            cancelled = false;
            return true;
        }
        return false;
    }

}
