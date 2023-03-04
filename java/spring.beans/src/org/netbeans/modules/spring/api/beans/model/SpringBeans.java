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

package org.netbeans.modules.spring.api.beans.model;

import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 * Encapsulates the root of a Spring config model. It provides access to the
 * list of bean definitions and useful methods for retrieving beans
 * by id, etc.
 *
 * @author Andrei Badea
 */
public interface SpringBeans {

    /**
     * Finds a bean by its id or name or alias.
     *
     * @param  idOrName the bean id or name or alias; never null.
     * @return the bean with the specified id or name; {@code null} if no such
     *         bean was found.
     */
    SpringBean findBean(String idOrName);

    /**
     * Returns the list of beans in the specified beans config file.
     *
     * @param  fo the beans config file.
     * @return the list of beans or {@code null} if {@code fo} was not
     *         used to create the contents of this {@code SpringBeans}.
     */
    FileSpringBeans getFileBeans(FileObject fo);

    /**
     * Returns the list of beans in the Spring config model.
     *
     * @return the list of beans; never {@code null}.
     */
    List<SpringBean> getBeans();
    
    /**
     * Returns all registered alias names in the Spring config model
     * 
     * @return registered aliases; never {@code null}.
     */
    Set<String> getAliases();
}
