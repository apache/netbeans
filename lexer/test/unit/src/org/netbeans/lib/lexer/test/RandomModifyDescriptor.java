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
