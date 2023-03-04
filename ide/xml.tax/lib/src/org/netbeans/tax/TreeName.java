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
package org.netbeans.tax;

/**
 * Immutable representation of <code>qName</code>.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeName {

    /**
     * The treeName prefix. For example, the prefix for the treeName "a:foo"
     * is "a".
     */
    private final String prefix;

    /**
     * The treeName name. For example, the name for the treeName "a:foo"
     * is "foo".
     */
    private final String name;


    /**
     * The treeName rawName. For example, the rawName for the treeName "a:foo"
     * is "a:foo".
     */
    private final String rawName;
    
    
    //
    // init
    //
    
    /** Creates new TreeName.
     * @throws InvalidArgumentException
     */
    public TreeName (String prefix, String name) throws InvalidArgumentException {
        checkPrefix (prefix);
        checkName (name);
        
        this.prefix  = prefix;
        this.name    = name;
        this.rawName = getQualifiedName (prefix, name);
    }
    
    /** Creates new TreeName.
     * @throws InvalidArgumentException
     */
    public TreeName (String rawName) throws InvalidArgumentException {
        checkRawName (rawName);
        
        this.prefix  = getPrefix (rawName);
        this.name    = getName (rawName);
        this.rawName = rawName;
    }
    
    //      /** Creates new TreeName -- copy constructor. */
    //      public TreeName (TreeName name) {
    //  	this.prefix  = name.prefix;
    //  	this.name    = name.name;
    //  	this.rawName = name.rawName;
    //      }
    
    
    //
    // itself
    //
    
    /**
     */
    private static String getPrefix (String rawName) {
        int i = rawName.indexOf (":"); // NOI18N
        
        if (i < 0) {
            return ""; // NOI18N
        } else {
            return rawName.substring (0, i);
        }
    }
    
    /**
     */
    private static String getName (String rawName) {
        int i = rawName.indexOf (":"); // NOI18N
        
        if (i < 0) {
            return rawName;
        } else {
            return rawName.substring (i + 1);
        }
    }
    
    /**
     */
    private static String getQualifiedName (String prefix, String name) {
        if ( "".equals (prefix) ) { // NOI18N
            return name;
        } else {
            return (prefix + ":" + name); // NOI18N
        }
        
    }
    
    /**
     */
    public String getPrefix () {
        return prefix;
    }
    
    /**
     */
    private void checkPrefix (String prefix) throws InvalidArgumentException {
        if ( prefix == null ) {
            throw createInvalidNullArgumentException ();
        }
    }
    
    /**
     */
    public String getName () {
        return name;
    }
    
    /**
     */
    private void checkName (String name) throws InvalidArgumentException {
        if ( name == null ) {
            throw createInvalidNullArgumentException ();
        }
    }
    
    /**
     * Should not it be just getQName() ???
     */
    public String getQualifiedName () {
        return rawName;
    }
    
    /**
     */
    private void checkRawName (String rawName) throws InvalidArgumentException {
        if ( rawName == null ) {
            throw createInvalidNullArgumentException ();
        }
    }
    
    /**
     */
    private InvalidArgumentException createInvalidNullArgumentException () {
        return new InvalidArgumentException
        (Util.THIS.getString ("EXC_invalid_null_value"),
        new NullPointerException ());
    }
    
    /**
     */
    public boolean equals (Object obj) {
        if ( obj instanceof TreeName ) {
            return rawName.equals (((TreeName)obj).rawName);
        }
        return false;
    }
    
    /**
     */
    public int hashCode () {
        return rawName.hashCode ();
    }
    
    /**
     */
    public String toString () {
        return rawName;
    }
    
}
