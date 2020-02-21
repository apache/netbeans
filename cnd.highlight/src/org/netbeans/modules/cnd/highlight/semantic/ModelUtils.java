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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 */
public class ModelUtils {

    public static final int HIGHLIGHT_DELAY = getInt("cnd.reparce.delay", 500); // NOI18N
    public static final int RESCHEDULE_HIGHLIGHT_DELAY = getInt("cnd.reschedule.task.delay", 500); // NOI18N

    public static final int OCCURRENCES_DELAY = getInt("cnd.reparce.delay", 300); // NOI18N
    public static final int RESCHEDULE_OCCURRENCES_DELAY = getInt("cnd.reschedule.task.delay", 300); // NOI18N

    public static final int SEMANTIC_DELAY = getInt("cnd.reparce.delay", 500); // NOI18N
    public static final int RESCHEDULE_SEMANTIC_DELAY = getInt("cnd.reschedule.task.delay", 500); // NOI18N

    private ModelUtils() {
    }

    private static int getInt(String name, int result){
        String text = System.getProperty(name);
        if( text != null ) {
            try {
                result = Integer.parseInt(text);
            } catch(NumberFormatException e){
                // default value
            }
        }
        return result;
    }


    /*package*/ static List<CsmReference> collect(final CsmFile csmFile, Document doc, final ReferenceCollector collector, final Interrupter interrupter) {
        CsmFileReferences.getDefault().accept(csmFile, doc, new CsmFileReferences.Visitor() {
            @Override
            public void visit(CsmReferenceContext context) {
                collector.visit(context.getReference(), csmFile);
            }

            @Override
            public boolean cancelled() {
                return interrupter.cancelled();
            }
        });
        return collector.getReferences();
    }

    /*package*/ static List<CsmOffsetable> getInactiveCodeBlocks(CsmFile file, Document doc, Interrupter interrupter) {
        return CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(file, interrupter);
    }

    /*package*/ static List<CsmReference> getMacroBlocks(CsmFile file, Document doc, Interrupter interrupter) {
        return CsmFileInfoQuery.getDefault().getMacroUsages(file, doc, interrupter);
    }

    private static abstract class AbstractReferenceCollector implements ReferenceCollector {
        protected final List<CsmReference> list;
        public AbstractReferenceCollector() {
            list = new ArrayList<>();
        }
        @Override
        public List<CsmReference> getReferences() {
            return list;
        }
    }

    /*package*/ static class FieldReferenceCollector extends AbstractReferenceCollector {
        private final Interrupter interrupter;

        public FieldReferenceCollector(Interrupter interrupter) {
            this.interrupter = interrupter;
        }
        public String getEntityName() {
            return "class-fields"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            if (!cancelled()) {
                CsmObject obj = ref.getReferencedObject();
                if (CsmKindUtilities.isField(obj)) {
                    list.add(ref);
                }
            }
        }

        @Override
        public boolean cancelled() {
            return interrupter.cancelled();
        }
    }

    /*package*/ static class TypedefReferenceCollector extends AbstractReferenceCollector {
        private final Interrupter interrupter;

        public TypedefReferenceCollector(Interrupter interrupter) {
            this.interrupter = interrupter;
        }
        public String getEntityName() {
            return "typedefs"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            if (!cancelled()) {
                CsmObject obj = ref.getReferencedObject();
                if (CsmKindUtilities.isTypedefOrTypeAlias(obj)) {
                    list.add(ref);
                }
            }
        }

        @Override
        public boolean cancelled() {
            return interrupter.cancelled();
        }
    }
    /*package*/ static class FunctionReferenceCollector extends AbstractReferenceCollector {
        private final Interrupter interrupter;

        public FunctionReferenceCollector(Interrupter interrupter) {
            this.interrupter = interrupter;
        }
        
        public String getEntityName() {
            return "functions-names"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            if (!cancelled()) {
                if (isWanted(ref, file)) {
                    list.add(ref);
                }
            }
        }
        private boolean isWanted(CsmReference ref, CsmFile file) {
            CsmObject csmObject = ref.getReferencedObject();
            return CsmKindUtilities.isFunction(csmObject);
        }

        @Override
        public boolean cancelled() {
            return interrupter.cancelled();
        }
    }

    /*package*/ static class EnumReferenceCollector extends AbstractReferenceCollector {
        private final Interrupter interrupter;

        public EnumReferenceCollector(Interrupter interrupter) {
            this.interrupter = interrupter;
        }
        
        public String getEntityName() {
            return "enums-names"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            if (!cancelled()) {
                if (isWanted(ref, file)) {
                    list.add(ref);
                }
            }
        }
        private boolean isWanted(CsmReference ref, CsmFile file) {
            CsmObject csmObject = ref.getReferencedObject();
            return CsmKindUtilities.isEnum(csmObject);
        }

        @Override
        public boolean cancelled() {
            return interrupter.cancelled();
        }
    }

    /*package*/ static class EnumeratorReferenceCollector extends AbstractReferenceCollector {
        private final Interrupter interrupter;

        public EnumeratorReferenceCollector(Interrupter interrupter) {
            this.interrupter = interrupter;
        }
        
        public String getEntityName() {
            return "enumerators-names"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            if (!cancelled()) {
                if (isWanted(ref, file)) {
                    list.add(ref);
                }
            }
        }
        private boolean isWanted(CsmReference ref, CsmFile file) {
            CsmObject csmObject = ref.getReferencedObject();
            return CsmKindUtilities.isEnumerator(csmObject);
        }

        @Override
        public boolean cancelled() {
            return interrupter.cancelled();
        }
    }

    /*package*/ static class ClassReferenceCollector extends AbstractReferenceCollector {
        private final Interrupter interrupter;

        public ClassReferenceCollector(Interrupter interrupter) {
            this.interrupter = interrupter;
        }
        
        public String getEntityName() {
            return "classes-names"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            if (!cancelled()) {
                if (isWanted(ref, file)) {
                    list.add(ref);
                }
            }
        }
        private boolean isWanted(CsmReference ref, CsmFile file) {
            CsmObject csmObject = ref.getReferencedObject();
            return CsmKindUtilities.isClass(csmObject);
        }

        @Override
        public boolean cancelled() {
            return interrupter.cancelled();
        }
    }

    /*package*/ static class UnusedVariableCollector implements ReferenceCollector {
        private final Map<CsmVariable, ReferenceCounter> counters;
        private Set<CsmParameter> parameters;
        private final Interrupter interrupter;
        
        public UnusedVariableCollector(Interrupter interrupter) {
            counters = new LinkedHashMap<>();
            this.interrupter = interrupter;
        }
        public String getEntityName() {
            return "unused-variables"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            if (!cancelled()) {
                CsmObject obj = ref.getReferencedObject();
                if (isWanted(obj, file)) {
                    CsmVariable var = (CsmVariable) obj;
                    ReferenceCounter counter = counters.get(var);
                    if (counter == null) {
                        counter = new ReferenceCounter(ref);
                        counters.put(var, counter);
                    } else {
                        counter.increment();
                    }
                }
            }
        }
        @Override
        public List<CsmReference> getReferences() {
            List<CsmReference> result = new ArrayList<>();
            for (ReferenceCounter counter : counters.values()) {
                if (counter.getCount() == 1) {
                    result.add(counter.getFirstReference());
                }
            }
            return result;
        }
        private boolean isWanted(CsmObject obj, CsmFile file) {
            if (!CsmKindUtilities.isLocalVariable(obj)) {
                // we want only local variables ...
                return false;
            }
            CsmVariable var = (CsmVariable)obj;
            if (!var.getContainingFile().equals(file)) {
                // ... only from current file
                return false;
            }
            if (CsmKindUtilities.isParameter(obj)) {
                CsmParameter prm = (CsmParameter) var;
                Set<CsmParameter> set = getFunctionDefinitionParameters(file);
                return set.contains(prm);
            } else {
                return true;
            }
        }
        private Set<CsmParameter> getFunctionDefinitionParameters(CsmFile file) {
            if (parameters == null) {
                parameters = new HashSet<>();
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.FUNCTION_DEFINITION, CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION);
                Iterator<CsmOffsetableDeclaration> i = CsmSelect.getDeclarations(file, filter);
                while (i.hasNext()) {
                    CsmFunctionDefinition fundef = (CsmFunctionDefinition)i.next();
                    parameters.addAll(fundef.getParameters());
                }
            }
            return parameters;
        }

        @Override
        public boolean cancelled() {
            return interrupter.cancelled();
        }
    }

    private static class ReferenceCounter {

        private CsmReference reference;
        private int count;

        public ReferenceCounter(CsmReference reference) {
            this.reference = reference;
            this.count = 1;
        }

        public CsmReference getFirstReference() {
            return reference;
        }

        public int getCount() {
            return count;
        }

        public void increment() {
            ++count;
            reference = null;
        }

    }

}
