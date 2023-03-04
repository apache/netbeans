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

package org.netbeans.modules.xml.multiview.ui;

import org.openide.util.NbBundle;

/**
 * A dialog to be shown when the editor is being closed the model is not valid.
 * Presents options for fixing, refreshing and saving. 
 *
 * Created on November 28, 2004, 7:18 PM
 * @author mkuchtiak
 */
public class RefreshSaveDialog extends org.openide.DialogDescriptor {
    public static final Integer OPTION_FIX=Integer.valueOf(0);
    public static final Integer OPTION_REFRESH=Integer.valueOf(1);
    public static final Integer OPTION_SAVE=Integer.valueOf(2);

    private static final String[] OPTIONS = new String[] {
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_FixNow"),
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_Refresh"),
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_Save")
    };

    /** Creates a new instance of RefreshSaveDialog */
    public RefreshSaveDialog(ErrorPanel errorPanel) {
        this (errorPanel, errorPanel.getErrorMessage());
    }
   
    /** Creates a new instance of RefreshSaveDialog */
    public RefreshSaveDialog(ErrorPanel errorPanel, String errorMessage ) {
        super (
            NbBundle.getMessage(RefreshSaveDialog.class,"TTL_warning_message",errorMessage),
            NbBundle.getMessage(RefreshSaveDialog.class,"TTL_warning"),
            true,
            OPTIONS,
            OPTIONS[0],
            BOTTOM_ALIGN,
            null,
            null
        );
        setButtonListener(new DialogListener(errorPanel));
        setClosingOptions(null);  
    }
    public Object getValue() {
        Object ret = super.getValue();
        if (ret.equals(OPTIONS[1])) return OPTION_REFRESH;
        else if (ret.equals(OPTIONS[2])) return OPTION_SAVE;
        else return OPTION_FIX;
    }
    
    private class DialogListener implements java.awt.event.ActionListener {
        private ErrorPanel errorPanel;
        DialogListener(ErrorPanel errorPanel) {
            this.errorPanel=errorPanel;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource().equals(OPTIONS[1]) || evt.getSource().equals(OPTIONS[2])) {
                errorPanel.clearError();
            }
        }
    }
}
