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

package org.netbeans.test.db.derby;

import org.netbeans.jellytools.JellyTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author lg198683
 */
public class DbJellyTestCase extends JellyTestCase {

    /** Creates a new instance of DbJellyTestCase */
    public DbJellyTestCase(String s) {
        super(s);
    }

    @Override
    public void setUp() {
        debug("########  "+getName()+"  #######");
    }
    
    protected void debug(String message) {
        log(message);
        System.out.println("> " + message);
    }

    protected void sleep(int millis) {
        debug("Waiting " + millis + " millis ... ");
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        debug("DONE");
    }
}
