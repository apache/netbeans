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

package org.netbeans.api.server;

import javax.swing.JComponent;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * The API representation of the single server instance. Class describes
 * the instance and provides needed operations.
 * <p>
 * Currently this class is not reachable through API methods. This can change
 * in future if we'll need to provide API in common server.
 * 
 * @author Petr Hejl
 */
public final class ServerInstance implements Lookup.Provider {

    static {
        ServerInstanceFactory.Accessor.DEFAULT = new ServerInstanceFactory.Accessor() {
            
            @Override
            public ServerInstance createServerInstance(ServerInstanceImplementation impl) {
                return new ServerInstance(impl);
            }
        };
    }
    
    private final ServerInstanceImplementation delegate;
    
    private ServerInstance(ServerInstanceImplementation delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the display name of the instance.
     *
     * @return the display name of the instance
     */    
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
    
    /**
     * Returns the display name of the server type to which this instance belongs.
     *
     * @return the display name of the server type to which this instance belongs
     */    
    public String getServerDisplayName() {
        return delegate.getServerDisplayName();
    }
    
    /**
     * Returns the node representing the runtime instance. The node should
     * display instance status and provide actions to manage the server.
     *
     * @return the node representing the instance, may return <code>null</code>
     */    
    public Node getFullNode() {
        return delegate.getFullNode();
    }    

    /**
     * Returns the node representing the instance while configuring it.
     * The node should not display any status, actions or children.
     *
     * @return the node representing the instance, may return <code>null</code>
     */    
    public Node getBasicNode() {
        return delegate.getBasicNode();
    }
    
    /**
     * Returns the component allowing the customization of the instance. May
     * return <code>null</code>.
     * <p>
     * Always called from Event Dispatch Thread.
     *
     * @return the component allowing the customization of the instance,
     *             may return <code>null</code>
     */
    public JComponent getCustomizer() {
        return delegate.getCustomizer();
    }
    
    /**
     * Removes the instance. No {@link org.netbeans.spi.server.ServerInstanceProvider}
     * should return this instance once it is removed.
     */    
    public void remove() {
        delegate.remove();
    }

    /**
     * Returns <code>true</code> if the instance can be removed by
     * {@link #remove()}. Otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if the instance can be removed
     */    
    public boolean isRemovable() {
        return delegate.isRemovable();
    }

    /**
     * Returns the lookup associated with this instance.
     *
     * @return the lookup associated with this instance
     * @since 1.19
     */
    public Lookup getLookup() {
        if (delegate instanceof Lookup.Provider) {
            return ((Lookup.Provider) delegate).getLookup();
        }
        return Lookup.EMPTY;
    }

    /**
     * Returns property value to which the specified <code>key</code> is mapped,
     * or <code>null</code> if this map contains no mapping for the
     * <code>key</code>.
     *
     * @param key server property <code>key</code>.
     * @return server property value or <code>null</code> if no value with given
     * <code>key</code> is stored.
     */
    public String getProperty(String key) {
        return delegate.getProperty(key);
    }
}
