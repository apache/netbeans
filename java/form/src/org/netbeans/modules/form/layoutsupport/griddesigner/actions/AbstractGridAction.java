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

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

import javax.swing.JMenuItem;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;


/**
 * Abstract {@code GridAction}.
 *
 * @author Jan Stola
 */
public abstract class AbstractGridAction implements GridAction {

    /**
     * Returns {@code null}.
     *
     * @param key not used.
     * @return {@code null}.
     */
    @Override
    public Object getValue(String key) {
        return null;
    }

    /**
     * Returns {@code true}.
     *
     * @param context not used.
     * @return {@code true}.
     */
    @Override
    public boolean isEnabled(DesignerContext context) {
        return true;
    }

    /**
     * Returns {@code null}.
     * 
     * @param performer not used.
     * @return {@code null}.
     */
    @Override
    public JMenuItem getPopupPresenter(GridActionPerformer performer) {
        return null;
    }

}
