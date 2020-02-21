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

import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import java.beans.PropertyChangeSupport;
import org.openide.nodes.Sheet;

import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.api.xml.*;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;

public final class EngineProfile extends ProfileSupport {

    public static final String PROFILE_ID = "nativedebugger"; // NOI18N

    public static final String PROP_ENGINE = "engine"; // NOI18N

    protected EngineProfile (PropertyChangeSupport pcs) {
	super(pcs);

        // See also comment in DebuggerManager for isChoosableEngine method
        engineType = EngineTypeManager.getOverrideEngineType();
        if (engineType == null) {
            engineType = EngineTypeManager.getFallbackEnineType();
        }
    }

    /**
     * Initializes the object to default values
     */
    @Override
    public void initialize() {
    }

    @Override
    public String getId() {
	return PROFILE_ID;
    }

    protected EngineType engineType = EngineTypeManager.getInherited();

    public void setEngineType(EngineType newEngineType) {
        engineType = newEngineType;
        needSave = true;
    }

    /**
     * Set the engine name by either it's toString() name or name() name.
     * The toString() form helps set it from userfacing property editors.
     * The name() form helps set it from XML persistence.
     * <br>
     * If engine name isn't recognized the defaultEngine value is used.
     * @param newEngineID ID of the engine type.
     */
    public EngineType setEngineByID(String newEngineID) {
       final EngineType oldEngine = engineType;

        engineType = EngineTypeManager.getEngineTypeByID(newEngineID);
        // clones don't have a pcs
        if (pcs != null) {
            pcs.firePropertyChange(PROP_ENGINE, oldEngine, engineType);
        }
        
        needSave = true;
        return engineType;
    }

    public EngineType getEngineType() {
        return engineType;
    }

    //
    // XML codec support
    // This stuff ends up in <projectdir>/nbproject/private/configurations.xml
    // 

    // interface ConfigurationAuxObject
    @Override
    public XMLDecoder getXMLDecoder() {
	return new EngineProfileXMLCodec(this);
    }

    // interface ConfigurationAuxObject
    @Override
    public XMLEncoder getXMLEncoder() {
	return new EngineProfileXMLCodec(this);
    }

    /**
     * Assign all values from a profileAuxObject to this object (reverse
     * of clone)
     */

    // interface ConfigurationAuxObject
    @Override
    public void assign(ConfigurationAuxObject profileAuxObject) {
	if (!(profileAuxObject instanceof EngineProfile)) {
	    // FIXUP: exception ????
	    System.err.print("Profile - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
	    return;
	}
	EngineProfile that = (EngineProfile) profileAuxObject;

	this.setValidatable(that.isValidatable());

	this.setEngineType(that.getEngineType());
    }

    

    /**
     * Clone itself to an identical (deep) copy.
     */

    // interface ConfigurationAuxObject
    @Override
    public ConfigurationAuxObject clone(Configuration conf) {
	EngineProfile clone = new EngineProfile (null);

	// don't clone pcs ... we'll end up notifying listeners prematurely
	// they will get notified on 'assign()'.

	clone.setValidatable(this.isValidatable());
	clone.setEngineType(this.getEngineType());
	return clone;
    }

    @Override
    public Sheet getSheet() {
        Sheet sheet = new Sheet();
        Sheet.Set set;

        set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(Catalog.get("GeneralTxt")); // NOI18N
        set.setShortDescription(Catalog.get("GeneralHint")); // NOI18N
	set.put(new EngineNodeProp(this));
	sheet.put(set);
	return sheet;
    }
}
