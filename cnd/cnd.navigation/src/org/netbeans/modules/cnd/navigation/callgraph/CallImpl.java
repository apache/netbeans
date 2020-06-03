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

package org.netbeans.modules.cnd.navigation.callgraph;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.text.PositionBounds;

/**
 *
 */
public class CallImpl implements Call {
    
    private final Function owner;
    private final Function callee;
    private final boolean nameOrder;
    private final int firstOccurrenceOffset;
    private final ArrayList<Occurrence> occurrences;
    private final CharSequence description;
    private final CharSequence htmlName;
    
    public CallImpl(CsmOffsetableDeclaration owner, ArrayList<CsmReference> references, CsmOffsetableDeclaration callee, boolean nameOrder) {
        this.nameOrder = nameOrder;
        this.owner = implementationResolver(owner);
        this.callee = implementationResolver(callee);
        this.occurrences = initOccurrences(references);
        this.firstOccurrenceOffset = references.get(0).getStartOffset();
        this.description = initDescription(references.get(0));
        this.htmlName = initHtmlDisplayName(references.get(0));
    }
    
    @Override
    public void open() {
        if (occurrences.isEmpty()) {
            return;
        }
        occurrences.get(0).open();
    }
    
    @Override
    public Function getCallee() {
        return callee;
    }

    @Override
    public Function getCaller() {
        return owner;
    }
    
    @Override
    public Collection<Occurrence> getOccurrences() {
        return occurrences;
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (htmlName != null) {
            return htmlName.toString();
        }
        return null;
    }

    @Override
    public String getDescription() {
        if (description != null) {
            return description.toString();
        }
        return null;
    }
    
    @Override
    public String toString() {
        if (nameOrder) {
            return getCallee().getName()+"<-"+getCaller().getName(); // NOI18N
        } else {
            return getCaller().getName()+"->"+getCallee().getName(); // NOI18N
        }
    }
    
    @Override
    public int compareTo(Call o) {
        if (nameOrder) {
            return getCaller().getName().compareTo(o.getCaller().getName());
        }
        int diff = firstOccurrenceOffset - ((CallImpl)o).firstOccurrenceOffset;
        if (diff == 0) {
            return getCallee().getName().compareTo(o.getCallee().getName());
        }
        return diff;
    }
    
    private CharSequence initHtmlDisplayName(CsmReference reference) {
        return CsmReferenceSupport.getContextLineHtml(reference, true);
    }

    private CharSequence initDescription(CsmReference reference) {
        return CsmReferenceSupport.getContextLine(reference);
    }
    
    private ArrayList<Occurrence> initOccurrences(ArrayList<CsmReference> references) {
        ArrayList<Occurrence> result = new ArrayList<Occurrence>(references.size());
        for (CsmReference ref : references) {
            result.add(new OccurrenceImpl(ref));
        }
        return result;
    }
    
    private static Function implementationResolver(CsmOffsetableDeclaration entity) {
        if (CsmKindUtilities.isFunction(entity)) {
            return new FunctionImpl((CsmFunction) entity);
        } else if (CsmKindUtilities.isVariable(entity)) {
            return new VariableImpl((CsmVariable) entity);
        } else if (CsmKindUtilities.isEnumerator(entity)) {
            return new VariableImpl((CsmEnumerator) entity);
        } else {
            return null;
        }
    }
    
    private static class OccurrenceImpl implements Call.Occurrence {
        private final PositionBounds positions;
        private final CharSequence description;
        private final CharSequence htmlName;
    
        private OccurrenceImpl(CsmReference reference) {
            positions = CsmUtilities.createPositionBounds(reference);
            description = initDescription(reference);
            htmlName = initHtmlDisplayName(reference);
        }
        
        @Override
        public void open() {
            CsmUtilities.openSource(positions);
        }
        
        @Override
        public String getHtmlDisplayName() {
            if (htmlName != null) {
                return htmlName.toString();
            }
            return null;
        }
        
        @Override
        public String getDescription() {
            if (description != null) {
                return description.toString();
            }
            return null;
        }
        
        private CharSequence initHtmlDisplayName(CsmReference ref) {
            return CsmReferenceSupport.getContextLineHtml(ref, true);
        }

        private CharSequence initDescription(CsmReference ref) {
            return CsmReferenceSupport.getContextLine(ref);
        }
    }
    
}
