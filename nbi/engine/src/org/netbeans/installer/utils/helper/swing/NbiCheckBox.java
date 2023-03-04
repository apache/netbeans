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

import javax.swing.JCheckBox;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiCheckBox extends JCheckBox {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public NbiCheckBox() {
        super();
        
        setText(DEFAULT_TEXT);
        setMnemonic(DEFAULT_MNEMONIC);
    }
    
    public void setText(String text) {
        super.setText(StringUtils.stripMnemonic(text));
        
        if (!SystemUtils.isMacOS()) {
            super.setMnemonic(StringUtils.fetchMnemonic(text));
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TEXT =
            ""; // NOI18N
    
    public static final char DEFAULT_MNEMONIC =
            '\u0000';
}
