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
package org.netbeans.modules.autoupdate.services;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.autoupdate.updateprovider.MessageDigestValue;

public class MessageDigestChecker {

    private static final Logger LOG = Logger.getLogger(MessageDigestChecker.class.getName());

    private final Map<String,MessageDigest> messageDigest = new HashMap<>();
    private final Map<String,byte[]> exptectedResult = new HashMap<>();
    private final Map<String,byte[]> calculatedResult = new HashMap<>();
    private Boolean overallValid = null;

    public MessageDigestChecker(Collection<MessageDigestValue> referenceHashes) {
        if (referenceHashes != null) {
            for (MessageDigestValue h : referenceHashes) {
                try {
                    MessageDigest md = MessageDigest.getInstance(h.getAlgorithm());
                    messageDigest.put(h.getAlgorithm(), md);
                    exptectedResult.put(h.getAlgorithm(), hexDecode(h.getValue()));
                } catch (NoSuchAlgorithmException ex) {
                    LOG.log(Level.FINE, "Unsupported Hash Algorithm: {0}", h.getAlgorithm());
                }
            }
        }
    }

    public void update(byte[] data) {
        ensureValidateNotCalled();
        for(MessageDigest md: messageDigest.values()) {
            md.update(data);
        }
    }

    public void update(byte[] data, int offset, int len) {
        ensureValidateNotCalled();
        for(MessageDigest md: messageDigest.values()) {
            md.update(data, offset, len);
        }
    }

    public void update(byte data) {
        ensureValidateNotCalled();
        for(MessageDigest md: messageDigest.values()) {
            md.update(data);
        }
    }

    private void ensureValidateNotCalled() throws IllegalStateException {
        if(overallValid != null) {
            throw new IllegalStateException("update must not be called after validate is invoked");
        }
    }

    private void ensureValidateCalled() throws IllegalStateException {
        if(overallValid == null) {
            throw new IllegalStateException("This method must not be called before validate is invoked");
        }
    }

    public boolean isDigestAvailable() {
        return ! messageDigest.isEmpty();
    }

    public boolean validate() throws IOException {
        if (overallValid == null) {
            overallValid = true;
            for (Entry<String, MessageDigest> e : messageDigest.entrySet()) {
                String algorithm = e.getKey();
                calculatedResult.put(algorithm, e.getValue().digest());
                boolean localValid = Arrays.equals(
                    exptectedResult.get(algorithm),
                    calculatedResult.get(algorithm));
                overallValid &= localValid;
            }
        }
        return overallValid;
    }

    public List<String> getFailingHashes() {
        ensureValidateCalled();
        List<String> result = new ArrayList<>();
        for(String algorithm: messageDigest.keySet()) {
            if(! Arrays.equals(
                exptectedResult.get(algorithm),
                calculatedResult.get(algorithm))) {
                result.add(algorithm);
            }
        }
        return result;
    }

    public String getExpectedHashAsString(String algorithm) {
        ensureValidateCalled();
        return hexEncode(exptectedResult.get(algorithm));
    }

    public String getCalculatedHashAsString(String algorithm) {
        ensureValidateCalled();
        return hexEncode(calculatedResult.get(algorithm));
    }

    public static String hexEncode(byte[] input) {
        StringBuilder sb = new StringBuilder(input.length * 2);
        for(byte b: input) {
            sb.append(Character.forDigit((b & 0xF0) >> 4, 16));
            sb.append(Character.forDigit((b & 0x0F), 16));
        }
        return sb.toString();
    }

    public static byte[] hexDecode(String input) {
        int length = input.length() / 2;
        byte[] result = new byte[length];
        for(int i = 0; i < length; i++) {
            int b = Character.digit(input.charAt(i * 2), 16) << 4;
            b |= Character.digit(input.charAt(i * 2 + 1), 16);
            result[i] = (byte) (b & 0xFF);
        }
        return result;
    }
}
