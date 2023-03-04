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

import java.awt.Insets;
import javax.swing.BorderFactory;

/**
 *
 * @author Jiri Sedlacek
 */
public class InvisibleToolbar extends GenericToolbar {

    public InvisibleToolbar() { super(); tweak(); }

    public InvisibleToolbar(int orientation) { super(orientation); tweak(); }

    public InvisibleToolbar(String name) { super(name); tweak(); }

    public InvisibleToolbar(String name, int orientation) { super(name, orientation); tweak(); }


    private void tweak() {
        setBorder(BorderFactory.createEmptyBorder());
        setBorderPainted(false);
        setFloatable(false);
        setRollover(true);
        setOpaque(false);
        setMargin(new Insets(0, 0, 0, 0));

        putClientProperty("Toolbar.noGTKBorder", Boolean.TRUE); // NOI18N
    }

}
