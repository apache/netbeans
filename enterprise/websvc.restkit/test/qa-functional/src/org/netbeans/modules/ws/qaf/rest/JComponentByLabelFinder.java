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
package org.netbeans.modules.ws.qaf.rest;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.jemmy.ComponentChooser;

/**
 *
 * @author lukas
 */
public class JComponentByLabelFinder implements ComponentChooser {

    private String label;

    public JComponentByLabelFinder(String label) {
        if (label == null || "".equals(label.trim())) { //NOI18N
            throw new IllegalArgumentException(label + " is not valid label"); //NOI18N
        }
        this.label = label.trim();
    }

    public boolean checkComponent(Component c) {
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            Object o = jc.getClientProperty("labeledBy"); //NOI18N
            if (o instanceof JLabel) {
                JLabel lbl = (JLabel) o;
                return label.equals(lbl.getText().trim());
            }
        }
        return false;
    }

    public String getDescription() {
        return "Find JComponent labeled by: " + label; //NOI18N
    }
}
