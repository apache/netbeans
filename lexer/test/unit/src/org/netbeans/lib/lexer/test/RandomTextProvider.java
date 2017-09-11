/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
