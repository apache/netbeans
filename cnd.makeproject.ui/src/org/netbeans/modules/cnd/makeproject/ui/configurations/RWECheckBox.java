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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JCheckBox;

/**
 *
 */
public class RWECheckBox extends JCheckBox {
    private int val = 0;

    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(16, 16);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(16, 16);
    }

    public int getVal() {
        if (isSelected())
            return val;
        else
            return 0;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
