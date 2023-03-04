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
package org.netbeans.modules.javascript2.editor.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.model.api.JsElement.Kind;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsReference;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author Petr Pisl
 */
public class OccurrencesFinderImpl extends OccurrencesFinder<JsParserResult> {

    private Map<OffsetRange, ColoringAttributes> range2Attribs;
    private int caretPosition;
    private volatile boolean cancelled;

    @Override
    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return range2Attribs;
    }

    @Override
    public void run(JsParserResult result, SchedulerEvent event) {
        //remove the last occurrences - the CSL caches the last found occurences for us
        range2Attribs = null;

        if(cancelled) {
            cancelled = false;
            return ;
        }
        int offset = result.getSnapshot().getEmbeddedOffset(caretPosition);
        Set<OffsetRange> ranges = findOccurrenceRanges(result, offset);
        range2Attribs = new HashMap<>();
        if(cancelled) {
            cancelled = false;
            return ;
        }
        for (OffsetRange offsetRange : ranges) {
            range2Attribs.put(ModelUtils.documentOffsetRange(result, offsetRange.getStart(), offsetRange.getEnd()), ColoringAttributes.MARK_OCCURRENCES);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    private static List<OffsetRange> findMemberUsage(JsObject object, String fqnType, String property, int offset, Set<String> processedObjects) {
        List<OffsetRange> result = new ArrayList<>();
        if (ModelUtils.wasProcessed(object, processedObjects)) {
            return Collections.emptyList();
        }

        String fqn = fqnType;
        if (fqn.endsWith(".prototype")) { // NOI18N
            fqn = fqn.substring(0, fqn.length() - 10);
        }
        Collection<? extends TypeUsage> assignments = object.getAssignments();
        if(!assignments.isEmpty()) {
            for(TypeUsage type : assignments) {
                if(type.getType().equals(fqn)) {
                    JsObject member = object.getProperty(property);
                    if(member != null) {
                        result.add(member.getDeclarationName().getOffsetRange());
                        List<Occurrence> occurrences = member.getOccurrences();
                        for (Occurrence occurence : occurrences) {
                            result.add(occurence.getOffsetRange());
                        }
                    }
                }
            }
        }
        if (!(object instanceof JsReference && ModelUtils.isDescendant(object, ((JsReference)object).getOriginal()))) {
            for(JsObject child : object.getProperties().values()) {
                result.addAll(findMemberUsage(child, fqn, property, offset, processedObjects));
            }
        }
        return result;
    }

    public static Set<OffsetRange> findOccurrenceRanges(JsParserResult result, int caretPosition) {
        Set<OffsetRange> offsets = new HashSet<>();
        Model model = Model.getModel(result, false);
        OccurrencesSupport os = new OccurrencesSupport(model);
        Occurrence occurrence = os.getOccurrence(caretPosition);
        if (occurrence != null) {

            for (JsObject object : occurrence.getDeclarations()) {
                if(object == null || object.getDeclarationName() == null) {
                    continue;
                }
                offsets.add(object.getDeclarationName().getOffsetRange());
                for (Occurrence oc : object.getOccurrences()) {
                    offsets.add(oc.getOffsetRange());
                }
                JsObject parent = object.getParent();
                if (parent != null && parent.getJSKind() != Kind.FILE && object.getJSKind() != Kind.PARAMETER
                        && !object.getModifiers().contains(Modifier.PRIVATE)) {
                    Collection<? extends Type> types = parent.getAssignmentForOffset(caretPosition);
                    if (types.isEmpty()) {
                        types = parent.getAssignments();
                    }
                    for (Type type : types) {
                        JsObject declaration = ModelUtils.findJsObjectByName(model, type.getType());
                        if (declaration != null && !object.getName().equals(declaration.getName())) {
                            JsObject prototype = declaration.getProperty(ModelUtils.PROTOTYPE);
                            declaration = declaration.getProperty(object.getName());
                            if (declaration == null && prototype != null) {
                                declaration = prototype.getProperty(object.getName());
                            }
                        }
                        if (declaration != null && !declaration.getModifiers().contains(Modifier.PRIVATE)) {
                            offsets.add(declaration.getDeclarationName().getOffsetRange());
                            for (Occurrence oc : declaration.getOccurrences()) {
                                offsets.add(oc.getOffsetRange());
                            }
                        }
                    }
                    if (types.isEmpty()) {
                        List<OffsetRange> usages = findMemberUsage(Model.getModel(result, false).getGlobalObject(), parent.getFullyQualifiedName(), object.getName(), caretPosition, new HashSet<>());
                        for (OffsetRange range : usages) {
                            offsets.add(range);
                        }
                    }
                }
            }

        }
        return offsets;
    }
}
