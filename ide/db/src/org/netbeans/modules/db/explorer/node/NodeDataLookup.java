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

package org.netbeans.modules.db.explorer.node;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * This is a Lookup that allowsdata instances to be easily
 * added and removed.
 * 
 * @author Rob Englander
 */
public class NodeDataLookup extends AbstractLookup {

    /** the data instances held in the lookup */
    private final Set<Object> dataInstances = Collections.synchronizedSet(new HashSet<Object>());

    /** the content of the underlying AbstractLookup */
    private final InstanceContent content;
    
    /**
     * Constructor
     */
    public NodeDataLookup() {
        this(new InstanceContent(), null);
    }
    
    /**
     * Constructor
     */
    public NodeDataLookup(Lookup lookup) {
        this(new InstanceContent(), lookup);
    }

    /**
     * This private constructor is used by the public constructor
     * so that the InstanceContent can be captured.
     * 
     * @param content the InstanceContent to construct the object with
     */
    private NodeDataLookup(InstanceContent content, Lookup lookup) {
        super(content);
        this.content = content;

        if (lookup != null) {
            Collection<? extends Object> objects = lookup.lookupAll(Object.class);
            for (Object obj : objects) {
                dataInstances.add(obj);
            }
            
            content.set(dataInstances, null);
        }
    }
    
    /**
     * Add an object instance to the lookup
     * 
     * @param data the data instance to be added
     */
    public void add(Object data) {
        dataInstances.add(data);
        content.set(dataInstances, null);
    }
    
}
