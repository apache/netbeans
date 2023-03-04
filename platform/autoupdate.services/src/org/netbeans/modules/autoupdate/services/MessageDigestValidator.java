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

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import org.netbeans.modules.autoupdate.services.Utilities;

public class MessageDigestValidator implements MessageValidator {

    private final MessageDigest messageDigest;
    private final byte[] expectedValue;
    private byte[] digestValue;

    public MessageDigestValidator(MessageDigest messageDigest, byte[] expectedValue) {
        Objects.requireNonNull(messageDigest, "messageDigenst must not be NULL");
        Objects.requireNonNull(expectedValue, "expectedValue must not be NULL");
        this.messageDigest = messageDigest;
        this.expectedValue = expectedValue;
    }

    @Override
    public void update(byte input) {
        if (digestValue != null) {
            throw new IllegalStateException("isValid was already called");
        }
        messageDigest.update(input);
    }

    @Override
    public void update(byte[] input, int offset, int len) {
        if (digestValue != null) {
            throw new IllegalStateException("isValid was already called");
        }
        messageDigest.update(input, offset, len);
    }

    @Override
    public void update(byte[] input) {
        if (digestValue != null) {
            throw new IllegalStateException("isValid was already called");
        }
        messageDigest.update(input);
    }

    @Override
    public void reset() {
        digestValue = null;
        messageDigest.reset();
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(getDigestValue(), expectedValue);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    private byte[] getDigestValue() {
        if (digestValue == null) {
            digestValue = messageDigest.digest();
        }
        return digestValue;
    }

    @Override
    public String getName() {
        return "Message Digest (" + messageDigest.getAlgorithm() + ")";
    }

    @Override
    public String getExpectedValueAsString() {
        if (expectedValue == null) {
            return "";
        } else {
            return Utilities.hexEncode(expectedValue);
        }
    }

    @Override
    public String getRealValueAsString() {
        if (expectedValue == null) {
            return "";
        } else {
            return Utilities.hexEncode(getDigestValue());
        }
    }
}
