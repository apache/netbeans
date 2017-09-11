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
    public static final Integer OPTION_FIX=new Integer(0);
    public static final Integer OPTION_REFRESH=new Integer(1);
    public static final Integer OPTION_SAVE=new Integer(2);

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
