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

package org.netbeans.lib.lexer.test;

import java.util.Random;

public class RandomTextProvider {

    private static final RandomCharDescriptor[] EMPTY_DESCRIPTORS = {};

    private static final FixedTextDescriptor[] EMPTY_FIXED_TEXTS = {};

    private RandomCharDescriptor[] randomCharDescriptors;

    private FixedTextDescriptor[] fixedTexts;

    private double ratioSum;

    private double fixedTextsRatioSum;

    public RandomTextProvider(RandomCharDescriptor[] randomCharDescriptors) {
        this(randomCharDescriptors, null);
    }

    public RandomTextProvider(RandomCharDescriptor[] randomCharDescriptors,
    FixedTextDescriptor[] fixedTexts) {
        if (randomCharDescriptors == null) {
            randomCharDescriptors = EMPTY_DESCRIPTORS;
        }
        if (fixedTexts == null) {
            fixedTexts = EMPTY_FIXED_TEXTS;
        }
        this.randomCharDescriptors = randomCharDescriptors;
        this.fixedTexts = fixedTexts;
        
        // Compute sum of ratios of all random char descriptors
        for (int i = 0; i < randomCharDescriptors.length; i++) {
            ratioSum += randomCharDescriptors[i].ratio();
        }
        for (int i = 0; i < fixedTexts.length; i++) {
            fixedTextsRatioSum += fixedTexts[i].ratio();
        }
    }
    
    public char randomChar(Random random) {
        double r = random.nextDouble() * ratioSum;
        for (int i = 0; i < randomCharDescriptors.length; i++) {
            RandomCharDescriptor descriptor = randomCharDescriptors[i];
            if ((r -= descriptor.ratio()) < 0) {
                return descriptor.randomChar(random);
            }
        }
        // Internal error - randomCharAvailable() needs to be checked
        throw new IllegalStateException("No random char descriptions available");
    }
    
    public boolean randomCharAvailable() {
        return (randomCharDescriptors.length > 0);
    }
    
    /**
     *
     * @return non-empty random string with length less or equal to maxTextLength.
     */
    public String randomText(Random random, int maxTextLength) {
        if (randomCharAvailable()) {
            int len = random.nextInt(maxTextLength);
            StringBuilder sb = new StringBuilder();
            while (--len >= 0) {
                sb.append(randomChar(random));
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    public String randomFixedText(Random random) {
        double r = random.nextDouble() * fixedTextsRatioSum;
        for (int i = 0; i < fixedTexts.length; i++) {
            FixedTextDescriptor fixedText = fixedTexts[i];
            if ((r -= fixedText.ratio()) < 0) {
                return fixedText.text();
            }
        }
        return ""; // no fixed texts available
    }

}
