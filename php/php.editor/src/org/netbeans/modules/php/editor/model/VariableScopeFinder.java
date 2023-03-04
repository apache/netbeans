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
package org.netbeans.modules.php.editor.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.model.impl.LazyBuild;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class VariableScopeFinder {

    private static final Logger LOGGER = Logger.getLogger(VariableScopeFinder.class.getName());

    public static VariableScopeFinder create() {
        return new VariableScopeFinder();
    }

    private VariableScopeFinder() {
    }

    public VariableScope find(final Scope scope, final int offset, final ScopeRangeAcceptor scopeRangeAcceptor) {
        assert scope != null;
        return find(scope.getElements(), offset, scopeRangeAcceptor);
    }

    public VariableScope find(final List<? extends ModelElement> elements, final int offset, final ScopeRangeAcceptor scopeRangeAcceptor) {
        AtomicBoolean isLazilyScanned = new AtomicBoolean(false);
        VariableScope variableScope = findWrapper(elements, offset, scopeRangeAcceptor, isLazilyScanned).getVariableScope();
        if (isLazilyScanned.get()) {
            // some scopes may be added new elements when LazyBuild elements are scanned.
            // so, find again.
            // e.g. Source instances are cached as weak references.
            // so, ParserResult may not be the same instance even if the FileObject is the same.
            // it means that new Model and new ModelVisitor are created again. in such case, LazyBuild elements may not be scanned yet.
            LOGGER.log(Level.FINE, "(LazyBuild)Scope is scanned."); // NOI18N
            if (isLazilyScanned.compareAndSet(true, false)) {
                variableScope = findWrapper(elements, offset, scopeRangeAcceptor, isLazilyScanned).getVariableScope();
            }
        }
        return variableScope;
    }

    private VariableScopeWrapper findWrapper(final List<? extends ModelElement> elements, final int offset, final ScopeRangeAcceptor scopeRangeAcceptor, @NullAllowed final AtomicBoolean isLazilyScanned) {
        assert elements != null;
        assert scopeRangeAcceptor != null;
        VariableScopeWrapper retval = VariableScopeWrapper.NONE;
        List<ModelElement> subElements = new LinkedList<>();
        for (ModelElement modelElement : elements) {
            if (modelElement instanceof VariableScope) {
                VariableScope varScope = (VariableScope) modelElement;
                VariableScopeWrapper varScopeWrapper = new VariableScopeWrapperImpl(varScope, scopeRangeAcceptor);
                if (scopeRangeAcceptor.accept(varScopeWrapper, offset) && scopeRangeAcceptor.overlaps(retval, varScopeWrapper)) {
                    // #268825
                    if (modelElement instanceof LazyBuild) {
                        LazyBuild scope = (LazyBuild) modelElement;
                        if (!scope.isScanned()) {
                            scope.scan();
                            if (isLazilyScanned != null) {
                                isLazilyScanned.set(true);
                            }
                        }
                    }
                    retval = varScopeWrapper;
                    subElements.addAll(varScopeWrapper.getElements());
                }
            }
        }
        VariableScopeWrapper subResult = subElements.isEmpty() ? VariableScopeWrapper.NONE : findWrapper(subElements, offset, scopeRangeAcceptor, isLazilyScanned);
        if (subResult == VariableScopeWrapper.NONE) {
            return retval;
        }
        // NETBEANS-4503
        return subResult.containsRange(retval) ? retval : subResult;
    }

    public VariableScope findNearestVarScope(Scope scope, int offset, VariableScope atOffset) {
        Collection<? extends ModelElement> elements = scope.getElements();
        for (ModelElement varScope : elements) {
            if (varScope instanceof ClassScope || varScope instanceof NamespaceScope) {
                atOffset = findNearestVarScope((Scope) varScope, offset, atOffset);
            }
            if (varScope instanceof VariableScope) {
                if (varScope.getNameRange().getStart() <= offset) {
                    if (atOffset == null || atOffset.getOffset() < varScope.getOffset()) {
                        FileObject fileObject = varScope.getFileObject();
                        if (fileObject == scope.getFileObject()) {
                            VariableScope variableScope = (VariableScope) varScope;
                            OffsetRange blockRange = variableScope.getBlockRange();
                            if (blockRange == null || blockRange.containsInclusive(offset)) {
                                atOffset = variableScope;
                            }
                        }
                    }
                }
            }

        }
        if (atOffset == null) {
            while (scope != null && !(scope instanceof VariableScope)) {
                scope = scope.getInScope();
            }
            if (scope != null) {
                OffsetRange blockRange = scope.getBlockRange();
                if (blockRange == null || blockRange.containsInclusive(offset)) {
                    atOffset = (VariableScope) scope;
                }
            }
        }
        return atOffset;
    }

    public interface ScopeRangeAcceptor {
        ScopeRangeAcceptor BLOCK = new ScopeRangeAcceptor() {

            @Override
            public boolean accept(VariableScopeWrapper variableScopeWrapper, int offset) {
                boolean result = false;
                OffsetRange blockRange = variableScopeWrapper.getBlockRange();
                if (blockRange != null && blockRange != OffsetRange.NONE) {
                    boolean possibleScope = true;
                    VariableScope variableScope = variableScopeWrapper.getVariableScope();
                    if (variableScope instanceof FunctionScope || variableScope instanceof ClassScope) {
                        if (blockRange.getEnd() == offset
                                && !(variableScope instanceof ArrowFunctionScope)) {
                            possibleScope = false;
                        }
                    }
                    result = possibleScope && blockRange.containsInclusive(offset);
                }
                return result;
            }

            @Override
            public boolean overlaps(VariableScopeWrapper old, VariableScopeWrapper young) {
                OffsetRange oldBlockRange = old.getBlockRange();
                OffsetRange youngBlockRange = young.getBlockRange();
                return old == VariableScopeWrapper.NONE || (oldBlockRange != null && youngBlockRange != null && oldBlockRange.overlaps(youngBlockRange));
            }
        };

        ScopeRangeAcceptor NAME_START_BLOCK_END = new ScopeRangeAcceptor() {

            @Override
            public boolean accept(VariableScopeWrapper variableScopeWrapper, int offset) {
                boolean result = BLOCK.accept(variableScopeWrapper, offset);
                if (!result) {
                    OffsetRange nameRange = variableScopeWrapper.getNameRange();
                    OffsetRange blockRange = variableScopeWrapper.getBlockRange();
                    if (nameRange != null & blockRange != null) {
                        OffsetRange allRange = new OffsetRange(nameRange.getStart(), blockRange.getStart());
                        result = allRange.containsInclusive(offset);
                    }
                }
                return result;
            }

            @Override
            public boolean overlaps(VariableScopeWrapper old, VariableScopeWrapper young) {
                OffsetRange oldNameRange = old.getNameRange();
                OffsetRange oldBlockRange = old.getBlockRange();
                OffsetRange oldRange = null;
                if (oldNameRange != null && oldBlockRange != null) {
                    oldRange = new OffsetRange(oldNameRange.getStart(), oldBlockRange.getStart());
                }
                OffsetRange youngNameRange = young.getNameRange();
                OffsetRange youngBlockRange = young.getBlockRange();
                OffsetRange youngRange = null;
                if (youngNameRange != null && youngBlockRange != null) {
                    youngRange = new OffsetRange(youngNameRange.getStart(), youngBlockRange.getStart());
                }
                return old == VariableScopeWrapper.NONE || BLOCK.overlaps(old, young)
                        || (oldRange != null && youngRange != null && oldRange.overlaps(youngRange));
            }
        };

        boolean accept(VariableScopeWrapper variableScopeWrapper, int offset);
        boolean overlaps(VariableScopeWrapper old, VariableScopeWrapper young);
    }

    public interface VariableScopeWrapper {
        VariableScopeWrapper NONE = new VariableScopeWrapper() {

            @Override
            public VariableScope getVariableScope() {
                return null;
            }

            @Override
            public boolean overlaps(VariableScopeWrapper variableScopeWrapper) {
                return true;
            }

            @Override
            public List<? extends ModelElement> getElements() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public OffsetRange getNameRange() {
                return null;
            }

            @Override
            public OffsetRange getBlockRange() {
                return null;
            }

            @Override
            public boolean containsRange(VariableScopeWrapper variableScopeWrapper) {
                return false;
            }
        };

        VariableScope getVariableScope();
        boolean overlaps(VariableScopeWrapper variableScopeWrapper);
        List<? extends ModelElement> getElements();
        OffsetRange getNameRange();
        OffsetRange getBlockRange();
        boolean containsRange(VariableScopeWrapper variableScopeWrapper);
    }

    private static final class VariableScopeWrapperImpl implements VariableScopeWrapper {
        private final VariableScope variableScope;
        private final ScopeRangeAcceptor scopeRangeAcceptor;

        private VariableScopeWrapperImpl(VariableScope variableScope, ScopeRangeAcceptor scopeRangeAcceptor) {
            assert variableScope != null;
            this.variableScope = variableScope;
            this.scopeRangeAcceptor = scopeRangeAcceptor;
        }

        @Override
        public VariableScope getVariableScope() {
            return variableScope;
        }

        @Override
        public boolean overlaps(VariableScopeWrapper variableScopeWrapper) {
            return scopeRangeAcceptor.overlaps(this, variableScopeWrapper);
        }

        @Override
        public List<? extends ModelElement> getElements() {
            return getVariableScope().getElements();
        }

        @Override
        public OffsetRange getNameRange() {
            return getVariableScope().getNameRange();
        }

        @Override
        public OffsetRange getBlockRange() {
            return getVariableScope().getBlockRange();
        }

        @Override
        public boolean containsRange(VariableScopeWrapper variableScopeWrapper) {
            OffsetRange baseRange = expandRange(getNameRange(), getBlockRange());
            OffsetRange targetRange = expandRange(variableScopeWrapper.getNameRange(), variableScopeWrapper.getBlockRange());
            return containsRange(baseRange, targetRange);
        }

        private static boolean containsRange(OffsetRange baseRange, OffsetRange targetRange) {
            if (baseRange == null || targetRange == null) {
                return false;
            }
            return baseRange.containsInclusive(targetRange.getStart())
                    && baseRange.containsInclusive(targetRange.getEnd());
        }

        @CheckForNull
        private static OffsetRange expandRange(OffsetRange range1, OffsetRange range2) {
            if (range1 == null && range2 == null) {
                return null;
            }
            if (range1 == null) {
                return range2;
            }
            if (range2 == null) {
                return range1;
            }
            return new OffsetRange(Math.min(range1.getStart(), range2.getStart()), Math.max(range1.getEnd(), range2.getEnd()));
        }

    }

}
