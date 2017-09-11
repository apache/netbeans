/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper.swing;

import javax.swing.JFileChooser;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class NbiFileChooser extends JFileChooser {
    
    public NbiFileChooser() {
        super();
        String titleProp = System.getProperty(FILECHOOSER_TITLE_PROPERTY);
        setDialogTitle((titleProp==null) ? DEFAULT_FILECHOOSER_TITLE : titleProp);
        
        String approveButtonProp = System.getProperty(FILECHOOSER_APPROVE_BUTTON_TEXT_PROPERTY);        
        String approveButtonText = (approveButtonProp==null) ? 
            DEFAULT_FILECHOOSER_APPROVE_BUTTON_TEXT : 
            approveButtonProp;
        
        if ((approveButtonText != null) && !approveButtonText.equals("")) {            
            setApproveButtonText(StringUtils.stripMnemonic(approveButtonText));
            setApproveButtonToolTipText(StringUtils.stripMnemonic(approveButtonText));            
            if (!SystemUtils.isMacOS()) {
                setApproveButtonMnemonic(StringUtils.fetchMnemonic(approveButtonText));
            }
        }
    }
    public static final String DEFAULT_FILECHOOSER_TITLE =
            ResourceUtils.getString(NbiFileChooser.class,
            "NFC.filechooser.title"); // NOI18N    
    public static final String DEFAULT_FILECHOOSER_APPROVE_BUTTON_TEXT =
            ResourceUtils.getString(NbiFileChooser.class,
            "NFC.filechooser.approve.button.text"); // NOI18N
    
    public static final String FILECHOOSER_TITLE_PROPERTY =
            "filechooser.title";
    public static final String FILECHOOSER_APPROVE_BUTTON_TEXT_PROPERTY =
            "filechooser.approve.button";
}
