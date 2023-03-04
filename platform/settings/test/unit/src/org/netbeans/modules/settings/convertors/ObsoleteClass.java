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

package org.netbeans.modules.settings.convertors;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;

/** Obsolete setting class
 *
 * @author  Jan Pokorsky
 */
public class ObsoleteClass implements java.io.Serializable {

    private static final long serialVersionUID = 3465637344523787865L;
    private String prop;

    public ObsoleteClass() {
    }
    public ObsoleteClass(String t) {
        prop = t;
    }

    private Object readResolve() throws ObjectStreamException {
        return new FooSetting(prop);
    }
    
}
