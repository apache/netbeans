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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.merge.builtin.visualizer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;

/**
 * This class controls the merge process.
 *
 * @author  Martin Entlicher
 */
public class MergeControl extends Object implements ActionListener, VetoableChangeListener {
    
    private Color colorUnresolvedConflict;
    private Color colorResolvedConflict;
    private Color colorOtherConflict;
    
    private MergePanel panel;
    private Difference[] diffs;
    /** The shift of differences */
    private int[][] diffShifts;
    /** The current diff */
    private int currentDiffLine = 0;
    private int[] resultDiffLocations;
    private final Map<Difference, AcceptKind> resolvedConflicts = new HashMap<Difference, AcceptKind>();
    private StreamSource resultSource;
    
    private boolean firstNewlineIsFake;
    private boolean secondNewlineIsFake;
    
    static enum AcceptKind {
        LEFT,
        RIGHT,
        LEFT_RIGHT,
        RIGHT_LEFT,
        NONE
    }

    /** Creates a new instance of MergeControl */
    public MergeControl(MergePanel panel) {
        this.panel = panel;
    }
    
    public void initialize(Difference[] diffs, StreamSource source1,
                           StreamSource source2, StreamSource result,
                           Color colorUnresolvedConflict, Color colorResolvedConflict,
                           Color colorOtherConflict) {
        if (diffs.length > 0) {
            // all lines must end with a newline
            Difference ld = diffs[diffs.length - 1];
            if (!ld.getFirstText().endsWith("\n")) {
                firstNewlineIsFake = true;
            }
            if (!ld.getSecondText().endsWith("\n")) {
                secondNewlineIsFake = true;
            }
            if (firstNewlineIsFake || secondNewlineIsFake) {
                diffs[diffs.length - 1] = new Difference(
                        ld.getType(), ld.getFirstStart(), ld.getFirstEnd(), ld.getSecondStart(), ld.getSecondEnd(), 
                        ld.getFirstText() + (firstNewlineIsFake ? "\n" : ""), 
                        ld.getSecondText() + (secondNewlineIsFake ? "\n" : ""));
            }
        }
        this.diffs = diffs;
        this.diffShifts = new int[diffs.length][2];
        this.resultDiffLocations = new int[diffs.length];
        for (Difference diff : diffs) {
            resolvedConflicts.put(diff, AcceptKind.NONE);
        }
        panel.setMimeType1(source1.getMIMEType());
        panel.setMimeType2(source2.getMIMEType());
        panel.setMimeType3(result.getMIMEType());
        panel.setSource1Title(source1.getTitle());
        panel.setSource2Title(source2.getTitle());
        panel.setResultSourceTitle(result.getTitle());
        panel.setName(source1.getName());
        try {
            panel.setSource1(source1.createReader());
            panel.setSource2(source2.createReader());
            panel.setResultSource(new java.io.StringReader(""));
        } catch (IOException ioex) {
            org.openide.ErrorManager.getDefault().notify(ioex);
        }
        this.colorUnresolvedConflict = colorUnresolvedConflict;
        this.colorResolvedConflict = colorResolvedConflict;
        this.colorOtherConflict = colorOtherConflict;
        insertEmptyLines(true);
        setDiffHighlight(true);
        copyToResult();
        panel.setConflicts(diffs);
        panel.addControlActionListener(this);
        showCurrentLine();
        this.resultSource = result;
    }
    
    private void insertEmptyLines(boolean updateActionLines) {
        int n = diffs.length;
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            if (updateActionLines && i < n - 1) {
                diffShifts[i + 1][0] = diffShifts[i][0];
                diffShifts[i + 1][1] = diffShifts[i][1];
            }
            switch (action.getType()) {
                case Difference.DELETE:
                    panel.addEmptyLines2(n3, n2 - n1 + 1);
                    if (updateActionLines && i < n - 1) {
                        diffShifts[i+1][1] += n2 - n1 + 1;
                    }
                    break;
                case Difference.ADD:
                    panel.addEmptyLines1(n1, n4 - n3 + 1);
                    if (updateActionLines && i < n - 1) {
                        diffShifts[i+1][0] += n4 - n3 + 1;
                    }
                    break;
                case Difference.CHANGE:
                    int r1 = n2 - n1;
                    int r2 = n4 - n3;
                    if (r1 < r2) {
                        panel.addEmptyLines1(n2, r2 - r1);
                        if (updateActionLines && i < n - 1) {
                            diffShifts[i+1][0] += r2 - r1;
                        }
                    } else if (r1 > r2) {
                        panel.addEmptyLines2(n4, r1 - r2);
                        if (updateActionLines && i < n - 1) {
                            diffShifts[i+1][1] += r1 - r2;
                        }
                    }
                    break;
            }
        }
    }
    
    private void setDiffHighlight(boolean set) {
        int n = diffs.length;
        //D.deb("Num Actions = "+n); // NOI18N
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            //D.deb("Action: "+action.getAction()+": ("+n1+","+n2+","+n3+","+n4+")"); // NOI18N
            switch (action.getType()) {
            case Difference.DELETE:
                if (set) panel.highlightRegion1(n1, n2, colorUnresolvedConflict);
                else panel.highlightRegion1(n1, n2, java.awt.Color.white);
                break;
            case Difference.ADD:
                if (set) panel.highlightRegion2(n3, n4, colorUnresolvedConflict);
                else panel.highlightRegion2(n3, n4, java.awt.Color.white);
                break;
            case Difference.CHANGE:
                if (set) {
                    panel.highlightRegion1(n1, n2, colorUnresolvedConflict);
                    panel.highlightRegion2(n3, n4, colorUnresolvedConflict);
                } else {
                    panel.highlightRegion1(n1, n2, java.awt.Color.white);
                    panel.highlightRegion2(n3, n4, java.awt.Color.white);
                }
                break;
            }
        }
    }
    
    private void copyToResult() {
        int n = diffs.length;
        int line1 = 1;
        int line3 = 1;
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            int endcopy = (action.getType() != Difference.ADD) ? (n1 - 1) : n1;
            //System.out.println("diff = "+n1+", "+n2+", "+n3+", "+n4+", endcopy = "+endcopy+((endcopy >= line1) ? "; copy("+line1+", "+endcopy+", "+line3+")" : ""));
            if (endcopy >= line1) {
                panel.copySource1ToResult(line1, endcopy, line3);
                line3 += endcopy + 1 - line1;
            }
            int length = Math.max(n2 - n1, 0) + Math.max(n4 - n3, 0);
            //System.out.println("  length = "+length+", addEmptyLines3("+line3+", "+(length + 1)+")");
            panel.addEmptyLines3(line3, length + 1);
            panel.highlightRegion3(line3, line3 + length, colorUnresolvedConflict);
            resultDiffLocations[i] = line3;
            line3 += length + 1;
            line1 = Math.max(n2, n4) + 1;
        }
        //System.out.println("copy("+line1+", -1, "+line3+")");
        panel.copySource1ToResult(line1, -1, line3);
    }

    private void showCurrentLine() {
        Difference diff = diffs[currentDiffLine];
        int line = diff.getFirstStart() + diffShifts[currentDiffLine][0];
        if (diff.getType() == Difference.ADD) line++;
        int lf1 = diff.getFirstEnd() - diff.getFirstStart() + 1;
        int lf2 = diff.getSecondEnd() - diff.getSecondStart() + 1;
        int length = Math.max(lf1, lf2);
        panel.setCurrentLine(line, length, currentDiffLine,
                             resultDiffLocations[currentDiffLine]);
    }
    
    /**
     * Resolve the merge conflict with left or right part.
     * This will reduce the number of conflicts by one.
     * @param right If true, use the right part, left otherwise
     * @param conflNum The number of conflict.
     */
    private void doResolveConflict(boolean right, int conflNum) {
        Difference diff = diffs[conflNum];
        int[] shifts = diffShifts[conflNum];
        int line1, line2, line3, line4;
        if (diff.getType() == Difference.ADD) {
            line1 = diff.getFirstStart() + shifts[0] + 1;
            line2 = line1 - 1;
        } else {
            line1 = diff.getFirstStart() + shifts[0];
            line2 = diff.getFirstEnd() + shifts[0];
        }
        if (diff.getType() == Difference.DELETE) {
            line3 = diff.getSecondStart() + shifts[1] + 1;
            line4 = line3 - 1;
        } else {
            line3 = diff.getSecondStart() + shifts[1];
            line4 = diff.getSecondEnd() + shifts[1];
        }
        //System.out.println("  diff lines = "+line1+", "+line2+", "+line3+", "+line4);
        int rlength = 0; // The length of the area before the conflict is resolved
        AcceptKind acceptedAs = resolvedConflicts.get(diff);
        switch (acceptedAs) {
            case NONE:
                rlength = Math.max(line2 - line1, 0) + Math.max(line4 - line3, 0);
                break;
            case LEFT:
                rlength = line2 - line1;
                break;
            case RIGHT:
                rlength = line4 - line3;
                break;
            case LEFT_RIGHT:
            case RIGHT_LEFT:
                rlength = line2 - line1 + line4 - line3 + 1;
                break;
        }
        int shift;
        if (right) {
            panel.replaceSource2InResult(line3, Math.max(line4, 0), // Correction for possibly negative value
                                         resultDiffLocations[conflNum],
                                         resultDiffLocations[conflNum] + rlength);
            shift = rlength - (line4 - line3);
            panel.highlightRegion1(line1, Math.max(line2, 0), colorOtherConflict);
            panel.highlightRegion2(line3, Math.max(line4, 0), colorResolvedConflict);
        } else {
            panel.replaceSource1InResult(line1, Math.max(line2, 0), // Correction for possibly negative value
                                         resultDiffLocations[conflNum],
                                         resultDiffLocations[conflNum] + rlength);
            shift = rlength - (line2 - line1);
            panel.highlightRegion1(line1, Math.max(line2, 0), colorResolvedConflict);
            panel.highlightRegion2(line3, Math.max(line4, 0), colorOtherConflict);
        }
        if (right && (line4 >= line3) || !right && (line2 >= line1)) {
            panel.highlightRegion3(resultDiffLocations[conflNum],
                                   resultDiffLocations[conflNum] + rlength - shift,
                                   colorResolvedConflict);
        } else {
            panel.unhighlightRegion3(resultDiffLocations[conflNum],
                                     resultDiffLocations[conflNum]);
        }
        for (int i = conflNum + 1; i < diffs.length; i++) {
            resultDiffLocations[i] -= shift;
        }
        resolvedConflicts.put(diff, right ? AcceptKind.RIGHT : AcceptKind.LEFT);
        panel.setNeedsSaveState(true);
    }
    
    private void doAcceptBoth (boolean right, int conflNum) {
        Difference diff = diffs[conflNum];
        int[] shifts = diffShifts[conflNum];
        int line1, line2, line3, line4;
        if (diff.getType() == Difference.ADD) {
            line1 = diff.getFirstStart() + shifts[0] + 1;
            line2 = line1 - 1;
        } else {
            line1 = diff.getFirstStart() + shifts[0];
            line2 = diff.getFirstEnd() + shifts[0];
        }
        if (diff.getType() == Difference.DELETE) {
            line3 = diff.getSecondStart() + shifts[1] + 1;
            line4 = line3 - 1;
        } else {
            line3 = diff.getSecondStart() + shifts[1];
            line4 = diff.getSecondEnd() + shifts[1];
        }
        //System.out.println("  diff lines = "+line1+", "+line2+", "+line3+", "+line4);
        int rlength = 0; // The length of the area before the conflict is resolved
        AcceptKind acceptedAs = resolvedConflicts.get(diff);
        switch (acceptedAs) {
            case NONE:
                rlength = Math.max(line2 - line1, 0) + Math.max(line4 - line3, 0);
                break;
            case LEFT:
                rlength = line2 - line1;
                break;
            case RIGHT:
                rlength = line4 - line3;
                break;
            case LEFT_RIGHT:
            case RIGHT_LEFT:
                rlength = line2 - line1 + line4 - line3 + 1;
                break;
        }
        int shift;
        panel.replaceBothInResult(line1, Math.max(line2, 0), line3, Math.max(line4, 0), // Correction for possibly negative value
                resultDiffLocations[conflNum],
                resultDiffLocations[conflNum] + rlength,
                right);
        shift = rlength - (line2 - line1 + line4 - line3 + 1);
        panel.highlightRegion1(line1, Math.max(line2, 0), colorResolvedConflict);
        panel.highlightRegion2(line3, Math.max(line4, 0), colorResolvedConflict);
        panel.highlightRegion3(resultDiffLocations[conflNum],
                resultDiffLocations[conflNum] + rlength - shift,
                colorResolvedConflict);
        for (int i = conflNum + 1; i < diffs.length; i++) {
            resultDiffLocations[i] -= shift;
        }
        resolvedConflicts.put(diff, right ? AcceptKind.RIGHT_LEFT : AcceptKind.LEFT_RIGHT);
        panel.setNeedsSaveState(true);
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        String actionCommand = actionEvent.getActionCommand();
        if (MergePanel.ACTION_FIRST_CONFLICT.equals(actionCommand)) {
            currentDiffLine = 0;
            showCurrentLine();
        } else if (MergePanel.ACTION_LAST_CONFLICT.equals(actionCommand)) {
            currentDiffLine = diffs.length - 1;
            showCurrentLine();
        } else if (MergePanel.ACTION_PREVIOUS_CONFLICT.equals(actionCommand)) {
            currentDiffLine--;
            if (currentDiffLine < 0) currentDiffLine = diffs.length - 1;
            showCurrentLine();
        } else if (MergePanel.ACTION_NEXT_CONFLICT.equals(actionCommand)) {
            currentDiffLine++;
            if (currentDiffLine >= diffs.length) currentDiffLine = 0;
            showCurrentLine();
        } else if (MergePanel.ACTION_ACCEPT_RIGHT.equals(actionCommand)) {
            doResolveConflict(true, currentDiffLine);
        } else if (MergePanel.ACTION_ACCEPT_LEFT.equals(actionCommand)) {
            doResolveConflict(false, currentDiffLine);
        } else if (MergePanel.ACTION_ACCEPT_RIGHT_LEFT.equals(actionCommand)) {
            doAcceptBoth(true, currentDiffLine);
        } else if (MergePanel.ACTION_ACCEPT_LEFT_RIGHT.equals(actionCommand)) {
            doAcceptBoth(false, currentDiffLine);
        }
    }
    
    public void vetoableChange(PropertyChangeEvent propertyChangeEvent) throws PropertyVetoException {
        if (MergeDialogComponent.PROP_PANEL_SAVE.equals(propertyChangeEvent.getPropertyName())) {
            MergePanel panel = (MergePanel) propertyChangeEvent.getNewValue();
            if (this.panel == panel) {
                ArrayList<Difference> unresolvedConflicts = new ArrayList<Difference>();//java.util.Arrays.asList(diffs));
                int diffLocationShift = 0;
                for (int i = 0; i < diffs.length; i++) {
                    if (resolvedConflicts.get(diffs[i]) == AcceptKind.NONE) {
                        int diffLocation = resultDiffLocations[i] - diffLocationShift;
                        Difference conflict = new Difference(diffs[i].getType(),
                                                             diffLocation,
                                                             diffLocation + diffs[i].getFirstEnd() - diffs[i].getFirstStart(),
                                                             diffLocation,
                                                             diffLocation + diffs[i].getSecondEnd() - diffs[i].getSecondStart(),
                                                             diffs[i].getFirstText(),
                                                             diffs[i].getSecondText());
                        unresolvedConflicts.add(conflict);
                        diffLocationShift += Math.max(diffs[i].getFirstEnd() - diffs[i].getFirstStart() + 1,
                                                      diffs[i].getSecondEnd() - diffs[i].getSecondStart() + 1);
                    }
                }
                try {
                    panel.writeResult(resultSource.createWriter(unresolvedConflicts.toArray(
                        new Difference[unresolvedConflicts.size()])), firstNewlineIsFake | secondNewlineIsFake);
                    panel.setNeedsSaveState(false);
                } catch (IOException ioex) {
                    PropertyVetoException pvex =
                        new PropertyVetoException(NbBundle.getMessage(MergeControl.class,
                                                                      "MergeControl.failedToSave",
                                                                      ioex.getLocalizedMessage()),
                                                  propertyChangeEvent);
                    pvex.initCause(ioex);
                    throw pvex;
                }
            }
        }
        if (MergeDialogComponent.PROP_PANEL_CLOSING.equals(propertyChangeEvent.getPropertyName())) {
            MergePanel panel = (MergePanel) propertyChangeEvent.getNewValue();
            if (this.panel == panel) {
                resultSource.close();
            }
        }
        if (MergeDialogComponent.PROP_ALL_CLOSED.equals(propertyChangeEvent.getPropertyName()) ||
            MergeDialogComponent.PROP_ALL_CANCELLED.equals(propertyChangeEvent.getPropertyName())) {
                resultSource.close();
        }
    }
    
}
