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

package org.netbeans.modules.web.core.palette.items;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JspPaletteUtilities;


/**
 *
 * @author Libor Kotouc
 */
public class SetProperty extends GetProperty {

    private String value = "";

    public SetProperty() {
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        allBeans = initAllBeans(targetComponent);
        SetPropertyCustomizer c = new SetPropertyCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                JspPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }

        return accept;
    }

    private String createBody() {
        String strBean = " name=\"\""; // NOI18N
        if (getBeanIndex() == -1) {
            strBean = " name=\"" + getBean() + "\""; // NOI18N
        } else {
            strBean = " name=\"" + allBeans.get(getBeanIndex()).getId() + "\""; // NOI18N
        }
        String strProperty = " property=\"" + getProperty() + "\""; // NOI18N
        String strValue = " value=\"" + getValue() + "\""; // NOI18N
        String sp = "<jsp:setProperty" + strBean + strProperty + strValue + " />"; // NOI18N
        return sp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
