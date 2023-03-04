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
package org.netbeans.modules.j2ee.sun.dd.api.cmp;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

public interface Consistency extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String NONE = "None"; // NOI18N
    public static final String CHECK_MODIFIED_AT_COMMIT = "CheckModifiedAtCommit"; // NOI18N
    public static final String LOCK_WHEN_LOADED = "LockWhenLoaded"; // NOI18N
    public static final String CHECK_ALL_AT_COMMIT = "CheckAllAtCommit"; // NOI18N
    public static final String LOCK_WHEN_MODIFIED = "LockWhenModified"; // NOI18N
    public static final String CHECK_VERSION_OF_ACCESSED_INSTANCES = "CheckVersionOfAccessedInstances"; // NOI18N

    public void setNone(boolean value);
    public boolean isNone();

    public void setCheckModifiedAtCommit(boolean value);
    public boolean isCheckModifiedAtCommit();

    public void setLockWhenLoaded(boolean value);
    public boolean isLockWhenLoaded();

    public void setCheckAllAtCommit(boolean value);
    public boolean isCheckAllAtCommit();

    public void setLockWhenModified(boolean value);
    public boolean isLockWhenModified();

    public void setCheckVersionOfAccessedInstances(CheckVersionOfAccessedInstances value) throws VersionNotSupportedException;
    public CheckVersionOfAccessedInstances getCheckVersionOfAccessedInstances() throws VersionNotSupportedException;
    public CheckVersionOfAccessedInstances newCheckVersionOfAccessedInstances() throws VersionNotSupportedException;

}
