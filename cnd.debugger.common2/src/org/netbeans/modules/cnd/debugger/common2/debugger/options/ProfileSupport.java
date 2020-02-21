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
