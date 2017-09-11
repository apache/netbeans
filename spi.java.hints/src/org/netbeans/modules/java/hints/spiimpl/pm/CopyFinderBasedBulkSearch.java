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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl.pm;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.AdditionalQueryConstraints;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.openide.util.Parameters;

/**
 *
 * @author lahvac
 */
public class CopyFinderBasedBulkSearch extends BulkSearch {

    public CopyFinderBasedBulkSearch() {
        super(false);
    }

    @Override
    public Map<String, Collection<TreePath>> match(CompilationInfo info, AtomicBoolean cancel, TreePath toSearch, BulkPattern pattern, Map<String, Long> timeLog) {
        Parameters.notNull("info", info);
        Map<String, Collection<TreePath>> result = new HashMap<String, Collection<TreePath>>();
        TreePath topLevel = new TreePath(info.getCompilationUnit());
        
        for (Entry<Tree, String> e : ((BulkPatternImpl) pattern).pattern2Code.entrySet()) {
            for (Occurrence od : Matcher.create(info).setCancel(new AtomicBoolean()).setUntypedMatching().setCancel(cancel).match(Pattern.createPatternWithFreeVariables(new TreePath(topLevel, e.getKey()), Collections.<String, TypeMirror>emptyMap()))) {
                Collection<TreePath> c = result.get(e.getValue());

                if (c == null) {
                    result.put(e.getValue(), c = new LinkedList<TreePath>());
                }

                c.add(od.getOccurrenceRoot());
            }
        }

        return result;
    }

    @Override
    public boolean matches(CompilationInfo info, AtomicBoolean cancel, TreePath toSearch, BulkPattern pattern) {
        //XXX: performance
        return !match(info, cancel, toSearch, pattern).isEmpty();
    }

    @Override
    public BulkPattern create(Collection<? extends String> code, Collection<? extends Tree> patterns, Collection<? extends AdditionalQueryConstraints> additionalConstraints, AtomicBoolean cancel) {
        Map<Tree, String> pattern2Code = new HashMap<Tree, String>();

        Iterator<? extends String> itCode = code.iterator();
        Iterator<? extends Tree>   itPatt = patterns.iterator();

        while (itCode.hasNext() && itPatt.hasNext()) {
            pattern2Code.put(itPatt.next(), itCode.next());
        }

        return new BulkPatternImpl(additionalConstraints, pattern2Code);
    }

    @Override
    public boolean matches(InputStream encoded, AtomicBoolean cancel, BulkPattern pattern) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void encode(Tree tree, EncodingContext ctx, AtomicBoolean cancel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Integer> matchesWithFrequencies(InputStream encoded, BulkPattern pattern, AtomicBoolean cancel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static final class BulkPatternImpl extends BulkPattern {

        private final Map<Tree, String> pattern2Code;
        
        public BulkPatternImpl(Collection<? extends AdditionalQueryConstraints> additionalConstraints, Map<Tree, String> pattern2Code) {
            super(new LinkedList<String>(pattern2Code.values()), null, null, new LinkedList<AdditionalQueryConstraints>(additionalConstraints));
            this.pattern2Code = pattern2Code;
        }

    }

}
