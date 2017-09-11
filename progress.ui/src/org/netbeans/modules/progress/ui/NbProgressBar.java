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

package org.netbeans.modules.progress.ui;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import org.netbeans.modules.progress.spi.ExtractedProgressUIWorker;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEvent;


/**
 * progress component, let just put the UI related issues here, update the state from outside

 * @author mkleint
 */
public class NbProgressBar extends JProgressBar implements ExtractedProgressUIWorker {
    
    static final String SLEEPY = "sleepy"; //NOI18N
    boolean isSetup = false;
    boolean usedInStatusBar = false;
    //TODO these two ought to be created only when the the bar is used externally..
    private JLabel detailLabel = new JLabel();
    private JLabel mainLabel = new JLabel();
    
    /** Creates a new instance of NbProgressBar */
    public NbProgressBar() {
        super();
        setOrientation(JProgressBar.HORIZONTAL);
        setAlignmentX(0.5f);
        setAlignmentY(0.5f);
        Color fg = UIManager.getColor ("nbProgressBar.Foreground");
        if (fg != null) {
            setForeground(fg);
        }
        Color bg = UIManager.getColor ("nbProgressBar.Background");
        if (bg != null) {
            setBackground(bg);
        }
    }
    
    public void setUseInStatusBar(boolean use) {
        usedInStatusBar = use;
    }
    
    public Dimension getPreferredSize() {
        Dimension supers = super.getPreferredSize();
        if (usedInStatusBar) {
            supers.width = ListComponent.ITEM_WIDTH / 3;
        }
        return supers;
    }

    
//--- these are used only when dealing with extracted component, when in status bar this is not used.    
//------------------------------------
    
    public void processProgressEvent(ProgressEvent event) {
        if (event.getType() == ProgressEvent.TYPE_START || !isSetup  || event.isSwitched()) {
            setupBar(event.getSource(), this);
            mainLabel.setText(event.getSource().getDisplayName());
            isSetup = true;
        } 
        if (event.getType() == ProgressEvent.TYPE_PROGRESS) {
            if (event.getWorkunitsDone() > 0) {
                setValue(event.getWorkunitsDone());
            }
            setString(StatusLineComponent.getBarString(event.getPercentageDone(), event.getEstimatedCompletion()));
            if (event.getDisplayName() != null) {
                mainLabel.setText(event.getDisplayName());
            }
            if (event.getMessage() != null) {
                detailLabel.setText(event.getMessage());
            }
            
        } else if (event.getType() == ProgressEvent.TYPE_FINISH) {
            boolean wasIndetermenite = isIndeterminate();
            setIndeterminate(false);
            setMaximum(event.getSource().getTotalUnits());
            setValue(event.getSource().getTotalUnits());
            if (wasIndetermenite) {
                setStringPainted(false);
            } else {
                setString(StatusLineComponent.getBarString(100, -1));
            }
        }
    }

    public void processSelectedProgressEvent(ProgressEvent event) {
        // ignore we'return always processing just one selected component
    }
    
    
    static void setupBar(InternalHandle handle, NbProgressBar bar) {
        bar.putClientProperty(SLEEPY, null); //NIO18N
        int total = handle.getTotalUnits();
        if (handle.isInSleepMode()) {
            bar.setStringPainted(true);
            bar.setIndeterminate(false);
            bar.setMaximum(1);
            bar.setMinimum(0);
            bar.setValue(0);
            bar.putClientProperty(SLEEPY, new Object()); //NIO18N
        } else if (total < 1) {
            // macosx workaround..            
            bar.setValue(bar.getMaximum());
            bar.setIndeterminate(true);
            bar.setStringPainted(false);
        } else {
            bar.setStringPainted(true);
            bar.setIndeterminate(false);
            bar.setMaximum(total);
            bar.setMinimum(0);
            bar.setValue(0);
        }
        bar.setString(" ");
    }    

    public JComponent getProgressComponent() {
        return this;
    }

    public JLabel getMainLabelComponent() {
        return mainLabel;
    }

    public JLabel getDetailLabelComponent() {
        return detailLabel;
    }
}
