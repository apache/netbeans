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

package org.netbeans.modules.cnd.api.picklist;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * DefaultPicklistElement
 */
public class DefaultPicklistElement implements PicklistElement, Serializable {
    private static final long serialVersionUID = -8893325364784938693L;
    private String elem;

    public DefaultPicklistElement(String elem) {
	this.elem = elem;
    }

    public String getString() {
	return elem;
    }

    /**
     * Compares two PicklistElement for equality. Returns true if equal,
     * otherwise false.
     */
    @Override
    public boolean equals(PicklistElement elem) {
	return ((DefaultPicklistElement)elem).getString().equals(this.elem);
    }

    /**
     * Returns a String representation of this element to be used
     * for displaying the element.
     */
    @Override
    public String displayName() {
	return elem;
    }

    /**
     * Return a clone (copy) of this element
     */
    @Override
    public PicklistElement cloneElement() {
	return new DefaultPicklistElement(elem);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	try {
	    out.writeObject(elem);
	}
	catch (IOException ioe) {
	    System.err.println("ExecutePicklistElement - writeObject - ioe " + ioe); // NOI18N
	    throw(ioe);
	}
    }
	                                                                                                         
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	try {
	    elem = (String)in.readObject();   
	}
	catch (IOException e) {
	    System.err.println("ExecutePicklistElement - readObject - e " + e); // NOI18N
	    throw(e);
	}
	catch (ClassNotFoundException e) {
	    System.err.println("ExecutePicklistElement - readObject - e " + e); // NOI18N
	    throw(e);
	}
    }
}
