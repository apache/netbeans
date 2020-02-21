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


package org.netbeans.modules.cnd.debugger.common2.utils.props;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;


/**
 * Explicit properties.
 *
 * The standard bean model has implicit properties that are 
 * based on naming conventions. There a property is typically implemented as
 * a private field and get/set/is accessors. Furthermore the set method
 * can fire property change notification.
 *
 * This class is a base class for explicit properties. For example:
 *	public IntegerProperty id =
 *		new IntegerProperty(this, "id", null, false, 0);
 *
 * 'owner' allows for registration of this property with an owner which, in
 * turn, provides iteration through properties and their lookup by name or key.
 *
 * 'name' is the name of the property and it's main purpose is for XML
 * persistence. It's looked up using equals().
 *
 * 'key' is used for binding the property to presentation layers like 
 * property sheets and tables and so on where the names might be
 * provided for us and/or follow this convention:
 *	public static final String PROP_BREAKPOINT_ID = "PROP_BREAKPOINT_ID"
 * such that quick, == based, lookup is preferred. Also because of the
 * final variable indirection the actual string may change and in such
 * cases any use of it in persistence will be troublesome.
 *
 * 'readonly' has to do with presentation to the user. The property is
 * programmatically always settable. readonly help remember wheter the user
 * can set it (through a property sheet etc).
 *
 * TBD:
 * One of the main motivations for using this scheme was to have a dirty bit
 * which is yet to be added. The dirty bit is an alternative approach to 
 * property change notifications and is easier to manage.
 *
 * A property can be gotten or set generically as a String or an Object.
 */

public abstract class Property {

    private final PropertyOwnerSupport owner;
    private final String name;
    private final String key;
    private final boolean readOnly;
    private /* LATER final */ boolean differentiating;

    private boolean dirty;

    protected Property(PropertyOwnerSupport owner,
		       String name, String key, boolean readOnly) {
	this.owner = owner;
	this.name = name;
	this.key = key;
	this.readOnly = readOnly;
	this.differentiating = true;

	owner.register(this);
    }

    public void setDifferentiating(boolean differentiating) {
	this.differentiating = differentiating;
    }

    public boolean isDifferentiating() {
	return differentiating;
    }

    public boolean isReadOnly() {
	return readOnly;
    }

    public String name() {
	return name;
    }

    public String key() {
	return key;
    }

    public boolean matches(Property that) {
	if (!IpeUtils.sameString(this.name, that.name))
	    return false;

	// if names are equal so should these
	assert this.readOnly == that.readOnly;
	assert IpeUtils.sameString(this.key, that.key);

	return IpeUtils.sameString(this.toString(), that.toString());
    }


    public abstract Object getAsObject();
    @Override
    public abstract String toString();

    public void setFromObject(Object o) {
	setFromObjectImpl(o);
	setDirty();
    }

    public void setFromObjectInitial(Object o) {
	setFromObjectImpl(o);
    }

    public void setFromString(String s) {
	setFromStringImpl(s);
	setDirty();
    }

    protected void setDirty() {
	owner.setDirty();
	this.dirty = true;
    }

    public boolean isDirty() {
	return dirty;
    }

    protected abstract void setFromObjectImpl(Object o);
    protected abstract void setFromStringImpl(String s);
}

