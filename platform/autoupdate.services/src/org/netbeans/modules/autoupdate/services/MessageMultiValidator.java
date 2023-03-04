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
package org.netbeans.modules.autoupdate.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MessageMultiValidator combines multiple validators and is only valid if all
 referenced validators report their checks as valid.
 */
public class MessageMultiValidator {

    private final List<MessageValidator> validators;

    public MessageMultiValidator(List<MessageValidator> validators) {
        this.validators = validators;
    }

    public MessageMultiValidator(MessageValidator... validators) {
        this.validators = Arrays.asList(validators);
    }

    public void update(byte[] input, int offset, int len) {
        this.validators.stream().forEach(v -> v.update(input, offset, len));
    }

    public void update(byte[] input) {
        this.validators.stream().forEach(v -> v.update(input));
    }

    public void update(byte input) {
        this.validators.stream().forEach(v -> v.update(input));
    }

    public void reset() {
        this.validators.stream().forEach(v -> v.reset());
    }

    public boolean isValid() {
        return this.validators.stream()
            .map(a -> a.isValid())
            .reduce(Boolean::logicalAnd)
            .orElse(true);
    }

    public Collection<MessageValidator> getValidators() {
        return Collections.unmodifiableList(this.validators);
    }
}
