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

        if (UIManager.getLookAndFeel().getID().startsWith("FlatLaf")) { //NOI18N
            putClientProperty("JProgressBar.largeHeight", use ? true : null); //NOI18N
        }
    }

    @Override
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
