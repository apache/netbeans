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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.AdditionalQueryConstraints;

/**
 *
 * @author lahvac
 */
public abstract class BulkSearch {

    private static final BulkSearch INSTANCE = new NFABasedBulkSearch();
//    private static final BulkSearch INSTANCE = new REBasedBulkSearch();

    public static BulkSearch getDefault() {
        return INSTANCE;
    }
    
    private final boolean requiresLightweightVerification;
    
    protected BulkSearch(boolean requiresLightweightVerification) {
        this.requiresLightweightVerification = requiresLightweightVerification;
    }
    
    public final Map<String, Collection<TreePath>> match(CompilationInfo info, AtomicBoolean cancel, TreePath toSearch, BulkPattern pattern) {
        return match(info, cancel, toSearch, pattern, null);
    }

    public final boolean requiresLightweightVerification() {
        return requiresLightweightVerification;
    }

    @CheckForNull
    public abstract Map<String, Collection<TreePath>> match(CompilationInfo info, AtomicBoolean cancel, TreePath toSearch, BulkPattern pattern, Map<String, Long> timeLog);

    public abstract boolean matches(InputStream encoded, AtomicBoolean cancel, BulkPattern pattern);
    
    @CheckForNull
    public abstract Map<String, Integer> matchesWithFrequencies(InputStream encoded, BulkPattern pattern, AtomicBoolean cancel);
    
    public abstract boolean matches(CompilationInfo info, AtomicBoolean cancel, TreePath toSearch, BulkPattern pattern);

    public abstract void encode(Tree tree, EncodingContext ctx, AtomicBoolean cancel);
    
    @CheckForNull
    public final BulkPattern create(CompilationInfo info, AtomicBoolean cancel, String... code) {
        return create(info, cancel, Arrays.asList(code));
    }

    @CheckForNull
    public final BulkPattern create(CompilationInfo info, AtomicBoolean cancel, Collection<? extends String> code) {
        List<Tree> patterns = new LinkedList<>();
        List<AdditionalQueryConstraints> additionalConstraints = new LinkedList<>();

        for (String c : code) {
            patterns.add(Utilities.parseAndAttribute(info, c, null));
            additionalConstraints.add(AdditionalQueryConstraints.empty());
        }

        return create(code, patterns, additionalConstraints, cancel);
    }
    
    @CheckForNull
    public abstract BulkPattern create(Collection<? extends String> code, Collection<? extends Tree> patterns, Collection<? extends AdditionalQueryConstraints> additionalConstraints, AtomicBoolean cancel);

    public abstract static class BulkPattern {

        private final List<? extends String> patterns;
        private final List<? extends Set<? extends String>> identifiers;
        private final List<List<List<String>>> requiredContent;
        private final List<AdditionalQueryConstraints> additionalConstraints;

        public BulkPattern(List<? extends String> patterns, List<? extends Set<? extends String>> identifiers, List<List<List<String>>> requiredContent, List<AdditionalQueryConstraints> additionalConstraints) {
            this.patterns = patterns;
            this.identifiers = identifiers;//TODO: immutable, maybe clone
            this.requiredContent = requiredContent;
            this.additionalConstraints = additionalConstraints;
        }

        public List<? extends String> getPatterns() {
            return patterns;
        }

        public List<? extends Set<? extends String>> getIdentifiers() {
            return identifiers;
        }

        public List<List<List<String>>> getRequiredContent() {
            return requiredContent;
        }

        public List<AdditionalQueryConstraints> getAdditionalConstraints() {
            return additionalConstraints;
        }

    }

    public static final class EncodingContext {

        private final OutputStream out;
        private final boolean forDuplicates;
        private Set<? extends String> identifiers;
        private List<String> content;

        public EncodingContext(OutputStream out, boolean forDuplicates) {
            this.out = out;
            this.forDuplicates = forDuplicates;
        }

        public Set<? extends String> getIdentifiers() {
            return identifiers;
        }

        public OutputStream getOut() {
            return out;
        }

        public boolean isForDuplicates() {
            return forDuplicates;
        }

        public void setIdentifiers(Set<? extends String> identifiers) {
            this.identifiers = identifiers;
        }

        public void setContent(List<String> content) {
            this.content = content;
        }

        public List<String> getContent() {
            return content;
        }

    }
}
