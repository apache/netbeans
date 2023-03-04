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
package org.netbeans.performance.benchmarks.bde;

/** Describes in integer interval */
public final class Interval {

    private int start;
    private int end;
    private int step;

    /** Creates new Interval */
    public Interval(int start, int end) {
        this(start, end, 1);
    }

    /** Creates new Interval */
    public Interval(int start, int end, int step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    /** @return start */
    public int getStart() {
        return start;
    }

    /** @return end */
    public int getEnd() {
        return end;
    }
    
    /** @return step */
    public int getStep() {
        return step;
    }
}
