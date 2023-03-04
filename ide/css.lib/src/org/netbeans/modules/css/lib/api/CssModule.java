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
package org.netbeans.modules.css.lib.api;

/**
 * Representation of CSS modules in terms of the w3c.org specifications.
 * 
 * Typically an instance of this class represents whole css3 spefication module.
 * 
 * see http://www.w3.org/Style/CSS/ for more information.
 *
 * @author mfukala@netbeans.org
 */
public interface CssModule {

    /** system id of the module. */
    public String getName();

    /** display name of the module - may be presented to users */
    public String getDisplayName();

    /** link to the css3 module specification. */
    public String getSpecificationURL();

}
