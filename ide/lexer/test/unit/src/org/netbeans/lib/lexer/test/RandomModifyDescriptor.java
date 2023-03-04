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

public class RandomModifyDescriptor {

    private final int opCount;

    private final RandomTextProvider randomTextProvider;

    private final double insertCharRatio;

    private final double insertTextRatio;

    private int insertTextMaxLength = 10;

    private final double insertFixedTextRatio;

    private final double removeCharRatio;

    private final double removeTextRatio;

    private int removeTextMaxLength = 10;

    private final double createSnapshotRatio;
    
    private final double destroySnapshotRatio;

    private double ratioSum;
    
    private boolean ratioSumInited;

    public RandomModifyDescriptor(int opCount, RandomTextProvider randomTextProvider,
    double insertCharRatio, double insertTextRatio, double insertFixedTextRatio,
    double removeCharRatio, double removeTextRatio,
    double createSnapshotRatio, double destroySnapshotRatio) {
        this.opCount = opCount;
        this.randomTextProvider = randomTextProvider;
        this.insertCharRatio = insertCharRatio;
        this.insertTextRatio = insertTextRatio;
        this.insertFixedTextRatio = insertFixedTextRatio;
        this.removeCharRatio = removeCharRatio;
        this.removeTextRatio = removeTextRatio;
        this.createSnapshotRatio = createSnapshotRatio;
        this.destroySnapshotRatio = destroySnapshotRatio;
    }
    
    protected double computeRatioSum() {
        return insertCharRatio + insertTextRatio + insertFixedTextRatio
                + removeCharRatio + removeTextRatio
                + createSnapshotRatio + destroySnapshotRatio;
    }
    
    public int opCount() {
        return opCount;
    }
    
    public RandomTextProvider randomTextProvider() {
        return randomTextProvider;
    }

    public double insertCharRatio() {
        return insertCharRatio;
    }
    
    public double insertTextRatio() {
        return insertTextRatio;
    }
    
    public int insertTextMaxLength() {
        return insertTextMaxLength;
    }
    
    public double insertFixedTextRatio() {
        return insertFixedTextRatio;
    }

    public void setInsertTextMaxLength(int insertTextMaxLength) {
        this.insertTextMaxLength = insertTextMaxLength;
    }
    
    public double removeCharRatio() {
        return removeCharRatio;
    }
    
    public double removeTextRatio() {
        return removeTextRatio;
    }
    
    public int removeTextMaxLength() {
        return removeTextMaxLength;
    }

    public void setRemoveTextMaxLength(int removeTextMaxLength) {
        this.removeTextMaxLength = removeTextMaxLength;
    }
    
    public double ratioSum() {
        if (!ratioSumInited) {
            ratioSumInited = true;
            ratioSum = computeRatioSum();
        }
        return ratioSum;
    }

    public double createSnapshotRatio() {
        return createSnapshotRatio;
    }
    
    public double destroySnapshotRatio() {
        return destroySnapshotRatio;
    }
    
}
