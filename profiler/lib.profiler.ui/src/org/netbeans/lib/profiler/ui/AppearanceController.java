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
package org.netbeans.lib.profiler.ui;

import javax.swing.JPanel;
import org.openide.util.Lookup;

/** Allows to control UI customizations by the NetBeans IDE, etc.
 * 
 * @since 1.147
 */
public class AppearanceController {
    private static final AppearanceController DEFAULT;
    static {
        AppearanceController ac = Lookup.getDefault().lookup(AppearanceController.class);
        DEFAULT = ac == null ? new AppearanceController() : ac;
    }

    public static AppearanceController getDefault() {
        return DEFAULT;
    }

    public void customizeProfilerTableContainer(JPanel p) {
    }

    public void customizeLiveFlatProfilePanel(JPanel p) {
    }

    public void customizeThreadPanel(JPanel p) {
    }

    public boolean isAddToRootsVisible() {
        return true;
    }

    public int[] invisibleLivenessResultsColumns() {
        return new int[] { 7 };
    }
}
