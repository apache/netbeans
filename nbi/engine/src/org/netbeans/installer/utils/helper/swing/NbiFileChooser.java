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
