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
        Map<String, Collection<TreePath>> result = new HashMap<>();
        TreePath topLevel = new TreePath(info.getCompilationUnit());
        
        for (Entry<Tree, String> e : ((BulkPatternImpl) pattern).pattern2Code.entrySet()) {
            for (Occurrence od : Matcher.create(info).setCancel(new AtomicBoolean()).setUntypedMatching().setCancel(cancel).match(Pattern.createPatternWithFreeVariables(new TreePath(topLevel, e.getKey()), Collections.<String, TypeMirror>emptyMap()))) {
                Collection<TreePath> c = result.get(e.getValue());

                if (c == null) {
                    result.put(e.getValue(), c = new LinkedList<>());
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
        Map<Tree, String> pattern2Code = new HashMap<>();

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
