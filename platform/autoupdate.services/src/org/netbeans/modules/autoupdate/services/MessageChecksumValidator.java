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

import java.util.Objects;
import java.util.zip.Checksum;

public class MessageChecksumValidator implements MessageValidator {

    private final Checksum checksum;
    private final long expectedValue;
    private Long digestValue;

    public MessageChecksumValidator(Checksum checksum, long expectedValue) {
        Objects.requireNonNull(checksum, "messageDigenst must not be NULL");
        Objects.requireNonNull(expectedValue, "expectedValue must not be NULL");
        this.checksum = checksum;
        this.expectedValue = expectedValue;
    }

    @Override
    public void update(byte b) {
        if (digestValue != null) {
            throw new IllegalStateException("isValid was already called");
        }
        checksum.update(b);
    }

    @Override
    public void update(byte[] b, int off, int len) {
        if (digestValue != null) {
            throw new IllegalStateException("isValid was already called");
        }
        checksum.update(b, off, len);
    }

    @Override
    public void update(byte[] input) {
        if (digestValue != null) {
            throw new IllegalStateException("isValid was already called");
        }
        update(input, 0, input.length);
    }

    @Override
    public boolean isValid() {
        return getDigestValue() == expectedValue;
    }

    private long getDigestValue() {
        if (digestValue == null) {
            digestValue = checksum.getValue();
        }
        return digestValue;
    }

    @Override
    public String getName() {
        return "Checksum " + checksum.getClass().getSimpleName();
    }

    @Override
    public String getExpectedValueAsString() {
        return Long.toString(expectedValue);
    }

    @Override
    public String getRealValueAsString() {
        return Long.toString(checksum.getValue());
    }

    @Override
    public void reset() {
        checksum.reset();
        digestValue = null;
    }
}
