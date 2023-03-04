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

package org.netbeans.lib.profiler.results.memory;


/**
 * This class is used to calculate the cardinality of the set of all object ages for the given class,
 * which is actually the definition of the number of surviving generations.
 *
 * @author Misha Dmitriev
 */
public class SurvGenSet {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] age;
    private int limit;
    private int nEls;
    private int nSlots;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SurvGenSet() {
        nSlots = 11;
        age = new int[nSlots];

        for (int i = 0; i < nSlots; i++) {
            age[i] = -1;
        }

        nEls = 0;
        limit = (nSlots * 3) / 4;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /* Get the total number of different ages */
    public int getTotalNoOfAges() {
        return nEls;
    }

    /**
     * Add the given age to the existing set of ages
     */
    public void addAge(int objAge) {
        int pos = objAge % nSlots;

        while ((age[pos] != objAge) && (age[pos] != -1)) {
            pos = (pos + 1) % nSlots;
        }

        if (age[pos] == -1) {
            age[pos] = objAge;
            nEls++;

            if (nEls >= limit) {
                rehash();
            }
        }
    }

    public void mergeWith(SurvGenSet other) {
        int[] otherAge = other.age;
        int otherLen = otherAge.length;

        for (int i = 0; i < otherLen; i++) {
            if (otherAge[i] != -1) {
                addAge(otherAge[i]);
            }
        }
    }

    private void rehash() {
        int[] oldAge = age;
        int oldNSlots = nSlots;
        nSlots = (oldNSlots * 2) + 1;
        age = new int[nSlots];

        for (int i = 0; i < nSlots; i++) {
            age[i] = -1;
        }

        nEls = 0;
        limit = (nSlots * 3) / 4;

        for (int i = 0; i < oldNSlots; i++) {
            if (oldAge[i] != -1) {
                addAge(oldAge[i]);
            }
        }
    }
}
