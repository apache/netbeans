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

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * Holds the textual value of an option and a dirty bit.
 */

public class OptionValue {

    private final OptionSetSupport owner;// Set to which we belong
    private final Option type;		// ... back pointer
    private final String defaultValue;	// initial value

    private String currValue;		// current value

    private boolean dirty = false;	// true when option has been changed
					// but not applied

    OptionValue(OptionSetSupport owner, Option type, String initialValue) {
	this.owner = owner;
	this.type = type;
	this.defaultValue = initialValue;

	currValue = initialValue;
    }

    String getDefaultValue() {
	return defaultValue;
    }

    public String get() {
	return currValue;
    } 

    public OptionSet owner() {
	return owner;
    }

    public Option type() {
	return type;
    } 

    /**
     * @return true if the value was truly set
     */

    private boolean setHelp(String newValue) {
	if (type.isTrim() && newValue != null)
	    newValue = newValue.trim();
	if (currValue == newValue)
	    return false;
	if (currValue != null && currValue.equals(newValue))
	    return false;
	currValue = newValue;
	return true;
    }

    void setInitialValue(String newValue) {
	setHelp(newValue);
    }

    public void set(String newValue) {
	if (setHelp(newValue)) {
	    setDirty(true);
	    owner.needSave();
	}
    }


    /**
     * Mark 'this' option as dirty if it's value differs from 'that'.
     */

    void deltaWithRespectTo(OptionValue that) {
	if (IpeUtils.sameString(this.currValue, that.currValue)) {
	    setDirty(false);
	} else {
	    setDirty(true);
	}
    }

    void applyTo(OptionClient client) {
	if (dirty) {
	    if (type().isClientOption())
		client.apply(this);
	    // OLD dirty = false;
	}
    }

    public void setEnabled(boolean enabled) {
	assert type.isYesNoOption();

	if (enabled)
	    set("on");		// NOI18N
	else
	    set("off");		// NOI18N
    }

    public boolean isEnabled() {
	assert type.isYesNoOption();
	if (get().equals("on"))	// NOI18N
	    return true;
	else
	    return false;
    }

    void setDirty(boolean dirty) {
	this.dirty = dirty;
    } 

    public boolean isDirty() {
	return dirty == true;
    }

    @Override
    public String toString() {
        return currValue + (dirty ? " (dirty)" : ""); //NOI18N
    }
}
