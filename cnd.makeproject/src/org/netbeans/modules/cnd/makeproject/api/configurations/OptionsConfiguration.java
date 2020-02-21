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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectOptionsFormat;

public class OptionsConfiguration implements Cloneable {
    private String preDefined = ""; // NOI18N
    private boolean dirty = false;

    private String commandLine;
    private boolean commandLineModified;

    // Constructors
    public OptionsConfiguration() {
	optionsReset();
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    // Options
    public void setValue(String commandLine) {
	this.commandLine = commandLine;
	setModified(!commandLine.equals(getDefault()));
    }
    public String getValue() {
	return commandLine;
    }
    public void setModified(boolean b) {
	this.commandLineModified = b;
    }
    public boolean getModified() {
	return commandLineModified;
    }
    public String getDefault() {
	return ""; // NOI18N
    }
    public void optionsReset() {
	commandLine = getDefault();
	commandLineModified = false;
    }

    public String getOptions(String prepend) {
	return MakeProjectOptionsFormat.reformatWhitespaces(getValue(), prepend);
    }

    public String[] getValues() {
        List<String> list = getValuesAsList();
	String[] values = new String[list.size()];
        int i = 0;
        for (String s : list) {
            values[i++] = s;
	}
        return values;
    }

    public List<String> getValuesAsList() {
        return MakeProjectOptionsFormat.tokenizeString(getValue());
    }

    // Predefined
    public void setPreDefined(String preDefined) {
	this.preDefined = preDefined;
    }
    public String getPreDefined() {
	return preDefined;
    }

    // Clone and assign
    public void assign(OptionsConfiguration conf) {
        final OptionsConfiguration confLocal = conf;
        if (confLocal == null || confLocal.getValue() == null) {
            return;
        }
        setDirty(!confLocal.getValue().equals(getValue()));
        setValue(confLocal.getValue());
        setModified(confLocal.getModified());
        //setDirty(conf.getDirty());
        setPreDefined(confLocal.getPreDefined());
    }

    @Override
    public OptionsConfiguration clone() {
        OptionsConfiguration clone = new OptionsConfiguration();
        clone.setValue(getValue());
        clone.setModified(getModified());
        clone.setDirty(getDirty());
        clone.setPreDefined(getPreDefined());
        return clone;
    }

    @Override
    public String toString() {
        return "{commandLine=" + commandLine + "] dirty=" + dirty + // NOI18N
                " commandLineModified=" + commandLineModified + '}'; // NOI18N
    }
}
