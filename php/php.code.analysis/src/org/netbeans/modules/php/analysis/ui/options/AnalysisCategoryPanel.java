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
package org.netbeans.modules.php.analysis.ui.options;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.validation.ValidationResult;

public abstract class AnalysisCategoryPanel extends JPanel {

    public abstract String getCategoryName();

    public abstract void addChangeListener(ChangeListener listener);

    public abstract void removeChangeListener(ChangeListener listener);

    public abstract void update();

    public abstract void applyChanges();

    public abstract boolean isChanged();

    public abstract ValidationResult getValidationResult();

}
