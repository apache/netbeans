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

package org.netbeans.modules.ant.freeform.spi;

/**
 * This interface is used to compute the help context for a freeform project.
 * Each {@link ProjectNature} should register an implementation in its lookup.
 * See {@link #getHelpIDFragment} to find out what are the requirements on the help
 * id fragments.
 *
 * If it is necessary to compute a help context for a freeform project, all
 * {@link HelpIDFragmentProvider}s registered in the project's lookup are asked to 
 * provide the fragments. The fragments are then lexicographically sorted and
 * concatenated (separated by dots) into one string, used as a base for the help id.
 *
 * @author Jan Lahoda
 * @since 1.11.1
 */
public interface HelpIDFragmentProvider {
    
    /**
     * Returns a help id fragment defined by the implementor. The method should return
     * the same string each time it is called (more preciselly, it is required that
     * <code>getHelpIDFragment().equals(getHelpIDFragment())</code>, but is allowed to
     * <code>getHelpIDFragment() != getHelpIDFragment()</code>). The string should be unique
     * among all the freeform project natures. The string is required to match this
     * regular expression: <code>([A-Za-z0-9])+</code>.
     *
     * Please note that the returned fragment is part of the contract between the
     * code and documentation, so be carefull when you need to change it.
     *
     * @return a non-null help id fragment, fullfilling the above conditions.
     */
    public String getHelpIDFragment();
    
}
