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
 * Holder for a prefix, URI pair.
 * <p>
 * Default namespace prefix is "".
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeNamespace {

    /** It is NOT in any namespace (including default) */
    public static final TreeNamespace NO_NAMESPACE  = new TreeNamespace (null, ""); // NOI18N

    /** */
    public static final TreeNamespace XML_NAMESPACE = new TreeNamespace ("xml", "http://www.w3.org/XML/1998/namespace"); // NOI18N

    /** */
    public static final TreeNamespace XMLNS_NAMESPACE = new TreeNamespace ("xmlns", "http://www.w3.org/2000/xmlns/"); // NOI18N
    
    /** For sticklers. */
    public static final String DEFAULT_NS_PREFIX = ""; // NOI18N
    
    /** */
    private String prefix;
    
    /** */
    private String uri;
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeNamespace.
     * @param prefix namespace prefix or null if no namespace (including default)
     * @param uri string representation of URI
     */
    protected TreeNamespace (String prefix, String uri) {
        if (uri == null) throw new IllegalArgumentException (Util.THIS.getString ("EXC_uri_cannot_be_null"));
        this.prefix = prefix;
        this.uri = uri;
    }
    
    /** Creates new TreeNamespace -- copy constructor. */
    protected TreeNamespace (TreeNamespace namespace) {
        this.prefix = namespace.prefix;
        this.uri    = namespace.uri;
    }
    
    
    //
    // itself
    //
    
    /**
     * @return prefix of null if no namespace
     */
    public String getPrefix () {
        return prefix;
    }
    
    /**
     * @return string representation URI (never null)
     */
    public String getURI () {
        return uri;
    }
    
}
