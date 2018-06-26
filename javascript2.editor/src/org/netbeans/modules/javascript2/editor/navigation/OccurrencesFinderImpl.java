/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
import org.netbeans.modules.javascript2.model.api.JsElement;
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
        range2Attribs = new HashMap<OffsetRange, ColoringAttributes>();
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
        List<OffsetRange> result = new ArrayList<OffsetRange>();
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
        Set<OffsetRange> offsets = new HashSet<OffsetRange>();
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
                        List<OffsetRange> usages = findMemberUsage(Model.getModel(result, false).getGlobalObject(), parent.getFullyQualifiedName(), object.getName(), caretPosition, new HashSet<String>());
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
