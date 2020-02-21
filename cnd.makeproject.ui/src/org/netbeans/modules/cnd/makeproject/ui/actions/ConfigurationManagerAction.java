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

package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.util.ResourceBundle;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
  */
public class ConfigurationManagerAction extends CallableSystemAction {
    @Override
    public void performAction() {
	CommonProjectActions.customizeProjectAction().actionPerformed(null);
    }

    @Override
    public String getName() {
	return getString("ConfigurationManagerAction"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
	return null;
    }

    @Override
    protected boolean asynchronous() {
	return true;
    }

    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(ConfigurationManagerAction.class);
	}
	return bundle.getString(s);
    }

}
