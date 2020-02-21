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

import java.util.ArrayList;
import java.util.Stack;

public class OptionLayers implements OptionSet {

    private Stack<OptionSet> layer = new Stack<OptionSet>();

    public OptionLayers(OptionSet bottommost) {
	assert bottommost != null :
	       "OptionLayers.<init>(): null bottommost";	// NOI18n // NOI18N
	layer.push(bottommost);
    }

    public void push(OptionSet next) {
	assert next != null :
	       "OptionLayers.push(): null next";	// NOI18n // NOI18N
	layer.push(next);
    }

    @Override
    public void save() {
	for (OptionSet os : layer)
	    os.save();
    }

    @Override
    public void open() {
	for (OptionSet os : layer)
	    os.open();
    }

    @Override
    public void markChanges() {
	for (OptionSet os : layer)
	    os.markChanges();
    }

    @Override
    public void doneApplying() {
	for (OptionSet os : layer)
	    os.doneApplying();
    }

    @Override
    public void applyTo(OptionClient client) {
	for (OptionSet os : layer)
	    os.applyTo(client);
    }

    @Override
    public boolean isDirty() {
	for (OptionSet os : layer) {
	    if (os.isDirty()) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public void clearDirty() {
	for (OptionSet os : layer)
	    os.clearDirty();
    }

    @Override
    public OptionValue byType(Option type) {
	OptionValue ov = null;
	for (OptionSet os : layer) {
	    ov = os.byType(type);
	    if (ov != null)
		break;
	}
	return ov;
    }

    @Override
    public OptionValue byName(String name) {
	OptionValue ov = null;
	for (OptionSet os : layer) {
	    ov = os.byName(name);
	    if (ov != null)
		break;
	}
	return ov;
    }

    @Override
    public void deltaWithRespectTo(OptionSet that) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void assign(OptionSet that) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void assignNonClient(OptionSet that) {
	throw new UnsupportedOperationException();
    }

    @Override
    public OptionSet makeCopy() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList<OptionValue> values() {
	throw new UnsupportedOperationException();
    }

    @Override
    public String tag() {
	throw new UnsupportedOperationException();
    }

    @Override
    public String description() {
	throw new UnsupportedOperationException();
    }
}
