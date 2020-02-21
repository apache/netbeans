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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.openide.nodes.PropertySupport;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

public final class SignalsNodeProp extends PropertySupport<Signals> {
    private final DbgProfile profile;

    public SignalsNodeProp(DbgProfile profile) {
	super("Signals",				// NOI18N
	      Signals.class,
	      Catalog.get("SignalsPropDisplayName"),	// NOI18N
	      Catalog.get("SignalsPropTT"),             // NOI18N
	      true, true);
	this.profile = profile;
    }

    @Override
    public Signals getValue() {
	return profile.signals();
    }

    @Override
    public void setValue(Signals v) {
	profile.signals().assign(v);
    }
	    
    private static PropertyEditor propertyEditor;

    @Override
    public PropertyEditor getPropertyEditor() {
	if (propertyEditor == null)
	    propertyEditor = new SignalsEditor();
	return propertyEditor;
    }

    // interface Node.Property
    @Override
    public boolean supportsDefaultValue() {
	return true;
    }

    // interface Node.Property
    @Override
    public boolean isDefaultValue() {
	return profile.signals().isDefaultValue();
    }

    // interface Node.Property
    @Override
    public void restoreDefaultValue() {
	profile.signals().restoreDefaultValue();
    }

    // interface Node.Property
    @Override
    public String getHtmlDisplayName() {
	if (isDefaultValue()) {
	    return getDisplayName();
	} else {
	    return "<b>" + getDisplayName() + "</b>";	// NOI18N
	}
    }

    @Override
    public Object getValue(String attributeName) {
	if (attributeName.equals("canEditAsText")) // NOI18N
	    return Boolean.FALSE;
	return super.getValue(attributeName);
    }

    private static final class SignalsEditor extends PropertyEditorSupport
				implements ExPropertyEditor {
	private PropertyEnv env;

	public SignalsEditor() {
	}

        @Override
	public void setAsText(String text) {
	}

        @Override
	public String getAsText() {
	    return getValue().toString();
	}

        @Override
	public java.awt.Component getCustomEditor () {

	    // Make a clone because the value edited by the editor might get
	    // discarded in case of cancellation.
	    // The new value will get assigned back to us by
	    // SignalsPanel.propertyChange().

	    Signals signals = (Signals) getValue();
	    SignalsPanel signalsPanel =
		new SignalsPanel(this, env, (Signals)signals.clone());
	    return signalsPanel;
	}

        @Override
	public boolean supportsCustomEditor () {
	    return true;
	}
	
	// interface ExPropertyEditor
        @Override
	public void attachEnv(PropertyEnv env) {
	    this.env = env;
	}
    }
}
