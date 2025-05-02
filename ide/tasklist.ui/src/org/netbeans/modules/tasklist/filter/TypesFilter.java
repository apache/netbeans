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

package org.netbeans.modules.tasklist.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author S. Aubrecht
 */
class TypesFilter {
    
    private Set<String> enabledProviders = new HashSet<String>();
    private int countLimit = 100;
    
    public TypesFilter() {
        addDefaultTypes();
    }
    
    private TypesFilter( TypesFilter src ) {
        this.countLimit = src.countLimit;
        this.enabledProviders.addAll( src.enabledProviders );
    }
    
    public boolean isEnabled( String type ) {
        return enabledProviders.contains( type );
    }
    
    public void setEnabled( String type, boolean enabled ) {
        if( enabled ) {
            enabledProviders.add( type );
        } else {
            enabledProviders.remove( type );
        }
    }
    
    public boolean isTaskCountLimitReached( int taskCount ) {
        return taskCount >= countLimit;
    }
    
    public void setTaskCountLimit( int limit ) {
        this.countLimit = limit;
    }
    
    public int getTaskCountLimit() {
        return this.countLimit;
    }
    
    public TypesFilter clone() {
        return new TypesFilter( this );
    }

    void clear() {
        enabledProviders.clear();
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        countLimit = prefs.getInt( prefix+"_countLimit", 100 ); //NOI18N
        enabledProviders.clear();
        String enabled = prefs.get( prefix+"_enabled", "" ); //NOI18N //NOI18N
        if( enabled.trim().length() > 0 ) {
            StringTokenizer tokenizer = new StringTokenizer( enabled, "\n" ); //NOI18N
            while( tokenizer.hasMoreTokens() ) {
                enabledProviders.add( tokenizer.nextToken() );
            }
        } else {
            addDefaultTypes();
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.putInt( prefix+"_countLimit", countLimit );
        StringBuffer buffer = new StringBuffer();
        for( Iterator<String> type = enabledProviders.iterator(); type.hasNext(); ) {
            buffer.append( type.next() );
            if( type.hasNext() )
                buffer.append( "\n" ); //NOI18N
        }
        prefs.put( prefix+"_enabled", buffer.toString() ); //NOI18N
    }
    
    private void addDefaultTypes() {
        enabledProviders.add("org.netbeans.modules.java.source.tasklist.JavaTaskProvider"); //NOI18N
        enabledProviders.add("org.netbeans.modules.tasklist.todo.TodoTaskScanner"); //NOI18N
        enabledProviders.add("org.netbeans.modules.csl.core.GsfTaskProvider"); //NOI18N
        enabledProviders.add("org.netbeans.modules.javafx.source.tasklist.JavaFXErrorTaskListProvider"); //NOI18N
        enabledProviders.add("org.netbeans.modules.java.editor.whitelist.WhiteListTaskProvider"); //NOI18N
    } 
} 
