/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tasklist.filter;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;

/**
 * Base class for all conditions that have a list of options to choos
 * frome the mode of operation, e.g. {Contains, Equals,..},
 * {Greater, Equal}, {True, NotTrue},etc.
 *
 * @author Tor Norbye
 * @author tl
 */
abstract class OneOfFilterCondition extends FilterCondition {
    
    private String[] options = null;
    private int id; // the selected option from nameKeys
    
    /**
     * Creates a condition with the given set of options and selected option.
     *
     * @param opts the set of options to choose from
     * @param id one of the constants from this class
     */
    public OneOfFilterCondition(String [] opts, int id) {
        this.options = opts;
        this.id = id;
    }
    
    public OneOfFilterCondition(final OneOfFilterCondition rhs) {
        super(rhs);
        this.options = rhs.options;
        this.id = rhs.id;
    }
    
    protected OneOfFilterCondition(String [] opts) {
        this.options = opts;
        this.id = 0;
    }
    
    
    @Override
    public boolean sameType(FilterCondition fc) {
        return super.sameType(fc) && this.id == ((OneOfFilterCondition)fc).id;
    }
    
    protected String getDisplayName() {
        return NbBundle.getMessage(this.getClass(), options[id]);
    }
    
    protected int getId() { return id;}
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        id = prefs.getInt( prefix+"_optionId", 0 ); //NOI18N
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.putInt( prefix+"_optionId", id ); //NOI18N
    }
}
