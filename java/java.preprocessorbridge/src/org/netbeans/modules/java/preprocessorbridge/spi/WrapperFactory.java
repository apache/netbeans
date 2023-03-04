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

package org.netbeans.modules.java.preprocessorbridge.spi;

import com.sun.source.util.Trees;

/**
 * This interface in a friend contract among the debugger/jpda/projects and java/source
 * module. The implementation provides wrappers for javac services which allows for
 * modifying the standard javac behavior.
 * Should be registered via {@link javax.swing.text.Document#putProperty(java.lang.Object, java.lang.Object)
 * Document.putProperty(WrapperFactory.class, factory)}.
 *
 * @author Dusan Balek
 * @since 1.14
 */
public interface WrapperFactory {

    /**
     * Returns wrapper for {@link Trees}
     * @param trees {@link Trees} instance being wrapped
     * @return non null wrapper
     */
    public Trees wrapTrees(Trees trees);
}
