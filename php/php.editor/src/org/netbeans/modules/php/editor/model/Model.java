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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;

/**
 * @author Radek Matous
 */
public final class Model {

    public enum Type {
        COMMON {
            @Override
            public void process(Model model) {
            }
        },
        EXTENDED {
            @Override
            public void process(Model model) {
                model.getExtendedElements();
            }
        };

        public abstract void process(Model model);
    }

    private static final Logger LOGGER = Logger.getLogger(Model.class.getName());

    private ModelVisitor modelVisitor;
    private final PHPParseResult info;
    private OccurencesSupport occurencesSupport;

    Model(PHPParseResult info) {
        this.info = info;
    }

    public List<PhpBaseElement>  getExtendedElements() {
        return getModelVisitor().extendedElements();
    }

    public FileScope getFileScope() {
        final ModelVisitor visitor = getModelVisitor();
        return visitor.getFileScope();
    }

    public IndexScope getIndexScope() {
        final ModelVisitor visitor = getModelVisitor();
        return visitor.getIndexScope();
    }

    public synchronized OccurencesSupport getOccurencesSupport(final OffsetRange range) {
        final ModelVisitor visitor = getModelVisitor();
        if (occurencesSupport == null || !range.containsInclusive(occurencesSupport.offset)) {
            occurencesSupport = new OccurencesSupport(visitor, range.getStart() + 1);
        }
        return occurencesSupport;
    }

    public synchronized OccurencesSupport getOccurencesSupport(final int offset) {
        final ModelVisitor visitor = getModelVisitor();
        if (occurencesSupport == null || occurencesSupport.offset != offset) {
            occurencesSupport = new OccurencesSupport(visitor, offset);
        }
        return occurencesSupport;
    }

    public ParameterInfoSupport getParameterInfoSupport(final int offset) {
        final ModelVisitor visitor = getModelVisitor();
        return new ParameterInfoSupport(visitor, offset);
    }

    public VariableScope getVariableScope(final int offset) {
        return getVariableScope(offset, VariableScopeFinder.ScopeRangeAcceptor.BLOCK);
    }

    /**
     * Tries to find the variable scope even when the caret is not directly in scope (block) ranges.
     *
     * E.g.: One has a caret inside a method name, so then the method Scope should be returned
     * even though it starts after the method name ends.
     *
     * @param offset
     * @return
     */
    public VariableScope getVariableScopeForNamedElement(final int offset) {
        return getVariableScope(offset, VariableScopeFinder.ScopeRangeAcceptor.NAME_START_BLOCK_END);
    }

    private VariableScope getVariableScope(final int offset, final VariableScopeFinder.ScopeRangeAcceptor scopeRangeAcceptor) {
        final ModelVisitor visitor = getModelVisitor();
        return visitor.getVariableScope(offset, scopeRangeAcceptor);
    }

    public ModelElement findDeclaration(final PhpElement element) {
        final ModelVisitor visitor = getModelVisitor();
        return visitor.findDeclaration(element);
    }

    /**
     * @return the modelVisitor
     */
    synchronized ModelVisitor getModelVisitor() {
        if (modelVisitor == null) {
            long start = System.currentTimeMillis();
            modelVisitor = new ModelVisitor(info);
            modelVisitor.scan(Utils.getRoot(info));
            long end = System.currentTimeMillis();
            LOGGER.log(Level.FINE, "Building model took: {0}", (end - start));
        }
        return modelVisitor;
    }

}
