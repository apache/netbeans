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
