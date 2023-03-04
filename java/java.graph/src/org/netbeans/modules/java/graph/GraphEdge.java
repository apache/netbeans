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

package org.netbeans.modules.java.graph;


/**
 *
 * @author Milos Kleint
 * @param <I>
 */
public final class GraphEdge<I extends GraphNodeImplementation> {
    private final String toString;
    private final I source;
    private final I target;
    private boolean primary;

    /** 
     * Creates a new instance of GraphEdge
     * @param source
     * @param target 
     */
    public GraphEdge(I source, I target) {
        toString = source.getQualifiedName() + "--" + target.getQualifiedName(); //NOI18N
        this.target = target;
        this.source = source;
    }
    
    @Override
    public String toString() {
        return toString;
    }

    public void setPrimaryPath(boolean primary) {
        this.primary = primary;
    }

    public boolean isPrimary() {
        return primary;
    }

    public I getSource() {
        return source;
    }

    public I getTarget() {
        return target;
    }

}
