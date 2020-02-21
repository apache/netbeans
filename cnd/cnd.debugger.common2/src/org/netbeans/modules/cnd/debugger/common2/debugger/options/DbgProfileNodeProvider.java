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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.nodes.Sheet;

import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.DebuggerCustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNodeProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = CustomizerNodeProvider.class)
public class DbgProfileNodeProvider implements CustomizerNodeProvider {
    // the name must be the same as used for debuggers in ProjectNodeFactory.createRootNodeProject
    private static final String DEBUGGER_NODE_ID = "Debug"; // NOI18N

    private static final String DEBUGGER_BUNDLE_ID = "Debug"; // NOI18N
    
    /**
     * Creates an instance of a customizer node
     */
    private CustomizerNode customizerNode = null;

    @Override
    public CustomizerNode factoryCreate(Lookup lookup) {
        if (customizerNode == null) {
            customizerNode = createDebugNode(lookup);
        }
        return customizerNode;
    }

    private CustomizerNode createDebugNode(Lookup lookup) {
	if (NativeDebuggerManager.isChoosableEngine()) {
            Collection<EngineType> engineTypes = EngineTypeManager.getEngineTypes(false);
	    Collection<CustomizerNode> childrenNodes = new ArrayList<CustomizerNode>(engineTypes.size());
            for (EngineType engineType : engineTypes) {
                CustomizerNode node = new DebugProfileGeneralCustomizerNode(
		    engineType.getDebuggerID(),
                    engineType,
		    engineType.getDisplayName(),
		    null, lookup);
                childrenNodes.add(node);
            }

            // chooser
	    CustomizerNode debugRootNode = new DebugProfileChoosableElementsCustomizerNode(
		    DEBUGGER_NODE_ID, 
		    getString(DEBUGGER_BUNDLE_ID),
		    childrenNodes.toArray(new CustomizerNode[childrenNodes.size()]), lookup);

	    return debugRootNode;
	} else {
	    CustomizerNode dbxRootNode = new DebugProfileGeneralCustomizerNode(
                    DEBUGGER_NODE_ID,
                    null,
                    getString(DEBUGGER_BUNDLE_ID),
                    null, lookup);

	    return dbxRootNode;
	}

    }

    private static final class DebugProfileGeneralCustomizerNode extends CustomizerNode implements DebuggerCustomizerNode {

        private final EngineType engineType;

        public DebugProfileGeneralCustomizerNode(String name, EngineType engineType, String displayName, CustomizerNode[] children, Lookup lookup) {
            super(name, displayName, children, lookup);
            this.engineType = engineType;
        }

        @Override
        public Sheet[] getSheets(Configuration configuration) {
            EngineType engine = engineType;
            if (engineType == null) {
                engine = NativeDebuggerManager.debuggerType(configuration);
            }
            String profileID = EngineTypeManager.engine2DebugProfileID(engine);
            DbgProfile dbgProfile = (DbgProfile) configuration.getAuxObject(profileID);
            return dbgProfile != null ? new Sheet[]{dbgProfile.getSheet()} : null;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsDebugging");
        }

        @Override
        public String getFamily() {
            return "SunStudio"; //NOI18N
        }
    }

    private static final class DebugProfileChoosableElementsCustomizerNode extends CustomizerNode implements DebuggerCustomizerNode {

	public DebugProfileChoosableElementsCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
	    super(name, displayName, children, lookup);
	}

        @Override
	public Sheet[] getSheets(Configuration configuration) {
            // show debugger chooser panel if it is enabled
            EngineProfile profile = (EngineProfile) configuration.getAuxObject(EngineProfile.PROFILE_ID);
            return profile != null ? new Sheet[]{profile.getSheet()} : null;
	}

        @Override
	public HelpCtx getHelpCtx() {
	    return new HelpCtx("ProjectPropsDebugging" );
	}

        @Override
        public String getFamily() {
            return "SunStudio"; //NOI18N
        }
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(DbgProfileNodeProvider.class);
	}
	return bundle.getString(s);
    }
}
