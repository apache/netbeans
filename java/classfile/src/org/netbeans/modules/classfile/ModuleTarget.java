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
package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * The JDK 9 ModuleTarget attribute.
 * @since 1.53
 * @author Tomas Zezula
 */
public final class ModuleTarget {
    private final String osName;
    private final String osArch;
    private final String osVersion;

    ModuleTarget(
        final DataInputStream in,
        final ConstantPool cp) throws IOException {
        int index = in.readUnsignedShort();
        this.osName = index == 0 ?
                null :
                ((CPUTF8Info)cp.get(index)).getName();
        index = in.readUnsignedShort();
        this.osArch = index == 0 ?
                null :
                ((CPUTF8Info)cp.get(index)).getName();
        index = in.readUnsignedShort();
        this.osVersion = index == 0 ?
                null :
                ((CPUTF8Info)cp.get(index)).getName();
    }

    /**
     * Returns the operating system name required by the module.
     * @return the operating system name or null if no information is present
     */
    public String getOSName() {
        return osName;
    }

    /**
     * Returns the operating system architecture required by the module.
     * @return the operating system architecture or null if no information is present
     */
    public String getOSArch() {
        return osArch;
    }

    /**
     * Returns the operating system version required by the module.
     * @return the operating system version or null if no information is present
     */
    public String getOSVersion() {
        return osVersion;
    }

    @Override
    public String toString() {
        return String.format(
                "os_name=%s, os_arch=%s, os_version=%s",    //NOI18N
                osName,
                osArch,
                osVersion);
    }
}
