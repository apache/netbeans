/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
