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
package org.netbeans.modules.selenium2.webclient.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider;
import org.openide.util.NbBundle;

public final class SeleniumTestingProviderRenderer implements ListCellRenderer<Object> {

    // @GuardedBy("EDT")
    private final ListCellRenderer<Object> defaultRenderer = new DefaultListCellRenderer();


    @NbBundle.Messages("SeleniumTestingProviderRenderer.none=<none>")
    @Override
    public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        assert EventQueue.isDispatchThread();
        String label;
        if (value instanceof SeleniumTestingProvider) {
            label = ((SeleniumTestingProvider) value).getDisplayName();
        } else {
            label = Bundle.SeleniumTestingProviderRenderer_none();
        }
        return defaultRenderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
    }

}
