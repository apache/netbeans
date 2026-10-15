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

package org.netbeans;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import org.openide.modules.Dependency;

public final class DepUtil {

    private static final MethodHandle create;

    // This is here to avoid having to make #create public or to export it via package-private split-package.
    static {
        try {
            Method createMethod = Dependency.class.getDeclaredMethod("create", String.class, String.class, int.class, int.class);
            createMethod.setAccessible(true);
            create = MethodHandles.publicLookup().unreflect(createMethod);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("can't access " + Dependency.class.getName() + "#create", ex);
        }
    }

    private DepUtil() {}

    /**
     * Reads a dependency from a stream.
     * <p>
     * Custom persistence for faster/ephemeral serialization.
     * <p>
     * Note: Do not use for long term persistence, format may change between releases.
     * @see #write(org.openide.modules.Dependency, java.io.DataOutput) 
     */
    public static Dependency read(DataInput is) throws IOException {
        int type = is.readByte();
        int comp = is.readByte();
        String name = is.readUTF();
        if (name == null || name.isEmpty()) {
            throw new IOException("name expected");
        }
        String version = is.readUTF();
        if (version.isEmpty()) {
            version = null;
        }
        try {
            return (Dependency) create.invokeExact(name, version, type, comp);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new IllegalStateException("could not invoke Dependendy#create", ex);
        }
    }

    /**
     * Writes this dependency to a stream.
     * <p>
     * Custom persistence for faster/ephemeral serialization.
     * <p>
     * Note: Do not use for long term persistence, format may change between releases.
     * @see #read(java.io.DataInput)
     */
    public static void write(Dependency dep, DataOutput os) throws IOException {
        os.writeByte(dep.getType());
        os.writeByte(dep.getComparison());
        os.writeUTF(dep.getName().strip());
        os.writeUTF(dep.getVersion() != null ? dep.getVersion().strip() : "");
    }
}
