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

package org.netbeans.modules.j2ee.sun.validation;

import org.netbeans.modules.schema2beans.BaseProperty;

/**
 * This Interface defines all the constants used in this framework
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public interface Constants {
    /***/
    int MANDATORY_ELEMENT = BaseProperty.INSTANCE_MANDATORY_ELT;

    int MANDATORY_ARRAY = BaseProperty.INSTANCE_MANDATORY_ARRAY;

    int OPTIONAL_ELEMENT = BaseProperty.INSTANCE_OPTIONAL_ELT;

    int OPTIONAL_ARRAY = BaseProperty.INSTANCE_OPTIONAL_ARRAY;

    String BUNDLE_FILE = 
            "org.netbeans.modules.j2ee.sun.validation.Bundle";          //NOI18N

    String IMPL_FILE = 
            "org.netbeans.modules.j2ee.sun.validation.impl.Impl";       //NOI18N
    
    String XPATH_DELIMITER = "/";                                       //NOI18N

    char XPATH_DELIMITER_CHAR = '/';
}
