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

package org.netbeans.api.debugger.jpda;


/**
 * Represents one Java thread group in debugged process.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Jan Jancura
 */
public interface JPDAThreadGroup  {


    /**
     * Getter for the name of thread group property.
     *
     * @return name of thread group
     */
    public abstract String getName ();

    /**
     * Returns parent thread group or null (for root thread group).
     *
     * @return parent thread group or null (for root thread group)
     */
    public abstract JPDAThreadGroup getParentThreadGroup ();
    
    /**
     * Returns this thread group's threads.
     *
     * @return threads from this thread group
     */
    public abstract JPDAThread[] getThreads ();
    
    /**
     * Returns this thread group's thread groups.
     *
     * @return thread groups s from this thread group
     */
    public abstract JPDAThreadGroup[] getThreadGroups ();
    
    /**
     * Suspends all threads and thread groups in this thread group.
     */
    public abstract void suspend ();
    
    /**
     * Unsuspends all threads and thread groups in this thread group.
     */
    public abstract void resume ();
}
