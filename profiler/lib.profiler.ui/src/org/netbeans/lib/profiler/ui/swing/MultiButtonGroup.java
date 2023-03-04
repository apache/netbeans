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
package org.netbeans.lib.profiler.ui.swing;

import java.util.HashSet;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/**
 * Button group with AT LEAST one button selected.
 *
 * @author Jiri Sedlacek
 */
public class MultiButtonGroup extends ButtonGroup {
        
    private final Set<ButtonModel> selected = new HashSet<>();

    public void setSelected(ButtonModel m, boolean b) {
        if (b == false) {
            for (ButtonModel model : selected) {
                if (model.isSelected() && model != m) {
                    selected.remove(m);
                    return;
                }
            }
        } else {
            selected.add(m);
        }
    }

    public boolean isSelected(ButtonModel m) {
        return selected.contains(m);
    }

}
