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

package org.netbeans.modules.java.hints.providers.spi;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.AdditionalQueryConstraints;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;

/**
 *
 * @author lahvac
 */
public class HintDescriptionFactory {

    private       HintMetadata metadata;
    private       Trigger trigger;
    private       Worker worker;
    private       AdditionalQueryConstraints additionalConstraints;
    private       String hintText;
    private       Set<Options> options;

    private HintDescriptionFactory() {
    }

    public static HintDescriptionFactory create() {
        return new HintDescriptionFactory();
    }

    /**TODO: move to create?
     *
     * @param metadata
     * @return
     */
    public HintDescriptionFactory setMetadata(HintMetadata metadata) {
        this.metadata = metadata;
        return this;
    }
    
    public HintDescriptionFactory setTriggerOptions(String[] triggerOpts) {
        this.trigger.setOptions(triggerOpts);
        return this;
    }

    public HintDescriptionFactory setTrigger(Trigger trigger) {
        if (this.trigger != null) {
            throw new IllegalStateException(this.trigger.toString());
        }

        this.trigger = trigger;
        return this;
    }

    public HintDescriptionFactory setWorker(Worker worker) {
        this.worker = worker;
        return this;
    }

    public HintDescriptionFactory setAdditionalConstraints(AdditionalQueryConstraints additionalConstraints) {
        this.additionalConstraints = additionalConstraints;
        return this;
    }

    public HintDescriptionFactory setHintText(@NonNull String hintText) {
        this.hintText = hintText;
        return this;
    }

    public HintDescriptionFactory addOptions(Options... options) {
        if (this.options == null) {
            this.options = EnumSet.noneOf(Options.class);
        }
        this.options.addAll(Arrays.asList(options));
        return this;
    }
        
    public HintDescription produce() {
        if (metadata == null) {
            metadata = HintMetadata.Builder.create("no-id").addOptions(Options.NON_GUI).build();
        }
        if (this.additionalConstraints == null) {
            this.additionalConstraints = AdditionalQueryConstraints.empty();
        }
        return new HintDescription(metadata, trigger, worker, additionalConstraints, hintText, options != null ? options : Set.of());
    }
    
}
