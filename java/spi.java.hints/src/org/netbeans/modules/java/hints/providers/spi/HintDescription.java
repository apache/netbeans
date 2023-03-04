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

package org.netbeans.modules.java.hints.providers.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author Jan Lahoda
 */
public final class HintDescription {

    private final HintMetadata metadata;
    private final Trigger trigger;
    private final Worker worker;
    private final AdditionalQueryConstraints additionalConstraints;
    private final String hintText;
    private final Set<Options> options;

    private HintDescription(HintMetadata metadata, Trigger trigger, Worker worker, AdditionalQueryConstraints additionalConstraints, String hintText, Set<Options> options) {
        this.metadata = metadata;
        this.trigger = trigger;
        this.worker = worker;
        this.additionalConstraints = additionalConstraints;
        this.hintText = hintText;
        this.options = options;
    }

    static HintDescription create(HintMetadata metadata, Trigger trigger, Worker worker, AdditionalQueryConstraints additionalConstraints, String hintText, Set<Options> options) {
        return new HintDescription(metadata, trigger, worker, additionalConstraints, hintText, options);
    }

    @Override
    public String toString() {
        return "[HintDescription:" + trigger + "]";
    }

    public AdditionalQueryConstraints getAdditionalConstraints() {
        return additionalConstraints;
    }

    public String getHintText() {
        return hintText;
    }

    public HintMetadata getMetadata() {
        return metadata;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Worker getWorker() {
        return worker;
    }

    public Set<Options> getOptions() {
        return options;
    }

    public static interface Worker {

        public Collection<? extends ErrorDescription> createErrors(HintContext ctx);

    }

    public static final class AdditionalQueryConstraints {
        public final Set<String> requiredErasedTypes;

        public AdditionalQueryConstraints(Set<String> requiredErasedTypes) {
            this.requiredErasedTypes = Collections.unmodifiableSet(new HashSet<>(requiredErasedTypes));
        }

        private static final AdditionalQueryConstraints EMPTY = new AdditionalQueryConstraints(Collections.<String>emptySet());
        public static AdditionalQueryConstraints empty() {
            return EMPTY;
        }
    }

}
