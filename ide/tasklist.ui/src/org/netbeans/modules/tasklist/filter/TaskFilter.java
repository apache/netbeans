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
import org.netbeans.modules.tasklist.impl.ScannerDescriptor;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;

/**
 *
 * @author S. Aubrecht
 */
public class TaskFilter {
    
    public static final TaskFilter EMPTY = new EmptyTaskFilter();
    
    private String name;
    private KeywordsFilter keywords;
    private TypesFilter types = new TypesFilter();
    
    TaskFilter( String name ) {
        this.name = name;
    }
    
    TaskFilter() {
    }
    
    private TaskFilter( TaskFilter src ) {
        this.name = src.name;
        keywords = null == src.keywords ? null : (KeywordsFilter)src.keywords.clone();
        types = null == src.types ? null : src.types.clone();
    }
     
    public boolean accept( Task task ) {
        return null == keywords ? true : keywords.accept( task );
    }
    
    public boolean isEnabled( FileTaskScanner scanner ) {
        return null == types ? true : types.isEnabled( ScannerDescriptor.getType( scanner ) );
    }
    
    public boolean isEnabled( PushTaskScanner scanner ) {
        return null == types ? true : types.isEnabled( ScannerDescriptor.getType( scanner ) );
    }
    
    public boolean isTaskCountLimitReached( int currentTaskCount ) {
        return null == types ? false : types.isTaskCountLimitReached( currentTaskCount );
    }
    
    public String getName() {
        return name;
    }
    
    void setName( String newName ) {
        this.name = newName;
    }
    
    KeywordsFilter getKeywordsFilter() {
        return keywords;
    }
    
    void setKeywordsFilter( KeywordsFilter f ) {
        this.keywords = f;
    }
    
    TypesFilter getTypesFilter() {
        return types;
    }
    
    void setTypesFilter( TypesFilter f ) {
        this.types = f;
    }
    
    @Override
    public Object clone() {
        return new TaskFilter( this );
    } 
    
    @Override
    public String toString() {
        return name;
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        name = prefs.get( prefix+"_name", "Filter" ); //NOI18N //NOI18N
        if( prefs.getBoolean( prefix+"_types", false ) ) { //NOI18N
            types = new TypesFilter();
            types.load( prefs, prefix+"_types" ); //NOI18N
        } else {
            types = null;
        }
        
        if( prefs.getBoolean( prefix+"_keywords", false ) ) { //NOI18N
            keywords = new KeywordsFilter();
            keywords.load( prefs, prefix+"_keywords" ); //NOI18N
        } else {
            keywords = null;
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.put( prefix+"_name", name ); //NOI18N
        
        if( null != types ) {
            prefs.putBoolean( prefix+"_types", true ); //NOI18N
            types.save( prefs, prefix+"_types" ); //NOI18N
        } else {
            prefs.putBoolean( prefix+"_types", false ); //NOI18N
        }
        
        if( null != keywords ) {
            prefs.putBoolean( prefix+"_keywords", true ); //NOI18N
            keywords.save( prefs, prefix+"_keywords" ); //NOI18N
        } else {
            prefs.putBoolean( prefix+"_keywords", false ); //NOI18N
        }
    }
    
    private static class EmptyTaskFilter extends TaskFilter {
        
        public EmptyTaskFilter() {
            super( Util.getString( "no-filter" ) ); //NOI18N
        }

        @Override
        public boolean accept(Task task) {
            return true;
        }

        @Override
        public boolean isEnabled(FileTaskScanner scanner) {
            return true;
        }

        @Override
        public boolean isEnabled(PushTaskScanner scanner) {
            return true;
        }

        @Override
        public boolean isTaskCountLimitReached(int currentTaskCount) {
            return false;
        }
    }
} 
