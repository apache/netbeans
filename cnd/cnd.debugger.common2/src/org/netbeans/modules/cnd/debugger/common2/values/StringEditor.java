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


package org.netbeans.modules.cnd.debugger.common2.values;

import java.awt.Color;
import javax.swing.JLabel;

public class StringEditor extends AsyncEditor {
    @Override
    public boolean isPaintable() {
	return true;
    } 

    private static class LabelHolder {
        static final JLabel label = new JLabel();
    };

    @Override
    public void paintValue(java.awt.Graphics g, java.awt.Rectangle box) {

        // 'label' instantiated on first use
        JLabel LABEL = LabelHolder.label;

	LABEL.setForeground(Color.black);
	LABEL.setFont(g.getFont());

        g.translate(box.x, box.y);
        Object o = getValue();
        if (o != null) {
            LABEL.setText(o.toString());
        } else {
            LABEL.setText("");                  // NOI18N
	}
        LABEL.setSize(box.width, box.height);
        LABEL.paint(g);
        g.translate(-box.x, -box.y);
    }
}
