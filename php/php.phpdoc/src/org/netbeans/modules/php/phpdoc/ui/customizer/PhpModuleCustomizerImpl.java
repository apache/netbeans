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
package org.netbeans.modules.php.phpdoc.ui.customizer;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.phpdoc.PhpDocumentorProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizer;

public final class PhpModuleCustomizerImpl implements PhpModuleCustomizer {

    private final PhpModule phpModule;

    private volatile PhpDocPanel panel;


    public PhpModuleCustomizerImpl(PhpModule phpModule) {
        this.phpModule = phpModule;
    }

    @Override
    public String getName() {
        return PhpDocumentorProvider.getInstance().getName();
    }

    @Override
    public String getDisplayName() {
        return PhpDocumentorProvider.getInstance().getDisplayName();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent();
        panel.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent();
        panel.removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        if (panel == null) {
            panel = new PhpDocPanel(phpModule);
        }
        return panel;
    }

    @Override
    public boolean isValid() {
        getComponent();
        return panel.isValidData();
    }

    @Override
    public String getErrorMessage() {
        getComponent();
        return panel.getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        getComponent();
        return panel.getWarningMessage();
    }

    @Override
    public void save() {
        getComponent();
        panel.storeData();
    }

    @Override
    public void close() {
        // noop
    }

}
