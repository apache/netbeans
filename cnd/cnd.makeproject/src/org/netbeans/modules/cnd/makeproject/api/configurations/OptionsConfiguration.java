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
