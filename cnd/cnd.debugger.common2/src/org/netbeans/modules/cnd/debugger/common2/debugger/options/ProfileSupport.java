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

/*
 * "ProfileSupport.java"
 * Common code for EngineProfile, DbxProfile and GdbProfile:
 *   - implement common methods for ConfigurationAuxObject, OptionSetOwner
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import java.beans.PropertyChangeSupport;

import org.openide.nodes.Sheet;

import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.api.xml.*;


public abstract class ProfileSupport implements ConfigurationAuxObject {

    protected final PropertyChangeSupport pcs;

    protected boolean needSave = false;

    /**
     * Constructor
     * Don't call this directly. It will get called when creating
     * ...cnd.execution.profiles.Profile().
     */
    public ProfileSupport() {
	pcs = null;
    }

    protected ProfileSupport(PropertyChangeSupport pcs) {
	this.pcs = pcs;
    }

    @Override
    public boolean shared() {
	return false;
    }

    // interface ConfigurationAuxObject
    @Override
    public abstract String getId();
    @Override
    public abstract XMLDecoder getXMLDecoder();
    @Override
    public abstract XMLEncoder getXMLEncoder();
    @Override
    public abstract void assign(ConfigurationAuxObject profileAuxObject);
    @Override
    public abstract ConfigurationAuxObject clone(Configuration conf);
    public abstract Sheet getSheet() ;

    // interface ConfigurationAuxObject
    @Override
    public void clearChanged() {
	needSave = false;
    }

    // interface ConfigurationAuxObject
    @Override
    public boolean hasChanged() {
	return needSave;
    }

    /**
     * Validatable property records whether there is an entity that can
     * validate UI changes, specifically an engine, listening on 'pcs'.
     */

    private boolean validatable;

    public void setValidatable(boolean validatable) {
	this.validatable = validatable;
    } 
    public boolean isValidatable() {
	return validatable;
    } 
}
