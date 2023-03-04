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

package org.netbeans.spi.java.classpath;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;

/**
 * The ClassPathImplementation with {@link ClassPath.Flag}s.
 * @author Tomas Zezula
 * @since 1.44
 */
public interface FlaggedClassPathImplementation extends ClassPathImplementation {

    /**
     * Name of the "flags" property.
     */
    public static final String PROP_FLAGS = "flags";    //NOI18N

    /**
     * Returns the {@link ClassPath}'s flags.
     * @return the {@link Flag}s
     */
    @NonNull
    Set<ClassPath.Flag> getFlags();
}
