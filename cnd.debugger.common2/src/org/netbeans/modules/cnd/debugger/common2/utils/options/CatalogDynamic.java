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

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.awt.Component;
import javax.swing.AbstractButton;

import org.openide.util.NbBundle;
import org.openide.ErrorManager;

/**
 * An instantiatable version of Catalog.
 */
public class CatalogDynamic {
    private Class cls;

    public CatalogDynamic(Class cls) {
	this.cls = cls;
    }

    public String get(String key) {
	String trans = key;
	try {
	    trans = NbBundle.getMessage(cls, key);
	} catch (java.util.MissingResourceException x) {
	    ErrorManager.getDefault().notify(x);
	}
	return trans;
    }

    public void setAccessibleDescription(Component component, String key) {
	component.getAccessibleContext().
	    setAccessibleDescription(get(key));
    }

    public void setAccessibleName(Component component, String key) {
	component.getAccessibleContext().
	    setAccessibleName(get(key));
    }

    public void setMnemonic(AbstractButton component, String key) {
	component.setMnemonic(get(key).charAt(0));
    }

    public char getMnemonic(String key) {
	return get(key).charAt(0);
    }
}
