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
package org.load3;

import java.util.logging.Logger;

public final class Load extends org.load2.Load {
    private static final Logger LOG = Logger.getLogger(Load.class.getName());
    
    /*
    static {
        String s = "org.load2.Load";
        try {
            LOG.info("static initializer 3 - waiting");
            Thread.sleep(500);
            LOG.info("static initializer 3 - waiting is over");
            Class<?> c = Class.forName(s, true, Load.class.getClassLoader());
            LOG.log(Level.INFO, "next class is loaded {0}", c);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    */
}
