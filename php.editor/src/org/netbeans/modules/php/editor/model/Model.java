/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
