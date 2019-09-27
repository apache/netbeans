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

package org.netbeans.modules.payara.spi;

/**
 *
 * @author Peter Williams
 */
public abstract class ResourceDecorator extends Decorator {

    /**
     * What property name does the delete resource command for this resource
     * type use.
     * 
     * @return property name to use to specify resource name in delete resource command
     */
    public abstract String getCmdPropertyName();

    /**
     * Does this resource's delete command support --cascade to remove dependent
     * resources (e.g. connection-pools)
     *
     * @return true if we should use cascade=true on delete
     */
    public boolean isCascadeDelete() {
        return false;
    }

    @Override
    public boolean canEditDetails() {
        return true;
    }

}
