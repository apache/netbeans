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

package org.netbeans.core.execution;

import java.security.PermissionCollection;
import java.security.Permission;
import java.util.Enumeration;

import org.openide.windows.InputOutput;

/** Every running process is represented by several objects in the ide whether
* or not it is executed as a thread or standalone process. The representation
* of a process should be marked by the IOPermissionCollection that gives possibility
* to such process to do its System.out/in operations through the ide.
*
* @author Ales Novak
*/
final class IOPermissionCollection extends PermissionCollection implements java.io.Serializable {

    /** InputOutput for this collection */
    private InputOutput io;
    /** Delegated PermissionCollection. */
    private PermissionCollection delegated;
    /** TaskThreadGroup ref or null */
    final TaskThreadGroup grp;

    static final long serialVersionUID =2046381622544740109L;
    /** Constructs new ExecutionIOPermission. */
    protected IOPermissionCollection(InputOutput io, PermissionCollection delegated, TaskThreadGroup grp) {
        this.io = io;
        this.delegated = delegated;
        this.grp = grp;
    }

    /** Standard implies method see java.security.Permission.
    * @param p a Permission
    */
    public boolean implies(Permission p) {
        return delegated.implies(p);
    }
    /** @return Enumeration of all Permissions in this collection. */
    public Enumeration<Permission> elements() {
        return delegated.elements();
    }
    /** @param perm a Permission to add. */
    public void add(Permission perm) {
        delegated.add(perm);
    }

    /** @return "" */ // NOI18N
    public InputOutput getIO() {
        return io;
    }
    /** Sets new io for this PermissionCollection */
    public void setIO(InputOutput io) {
        this.io = io;
    }

    @Override
    public String toString() {
        return delegated.toString();
    }
}
