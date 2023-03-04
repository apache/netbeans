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

package org.netbeans.modules.php.zend.util;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tomas Mysik
 */
public class ZendUtilsTest extends NbTestCase {

    public ZendUtilsTest(String name) {
        super(name);
    }

    @Test
    public void testControllerName() {
        assertEquals("IndexController", ZendUtils.getControllerName("index"));
        assertEquals("AllJobsController", ZendUtils.getControllerName("all-jobs"));
    }

    @Test
    public void testActionName() {
        assertEquals("indexAction", ZendUtils.getActionName("index"));
        assertEquals("allJobsAction", ZendUtils.getActionName("all-jobs"));
    }

    @Test
    public void testViewName() {
        assertEquals("index", ZendUtils.getViewName("indexAction"));
        assertEquals("all-jobs", ZendUtils.getViewName("allJobsAction"));
    }

    @Test
    public void testViewFolderName() {
        assertEquals("index", ZendUtils.getViewFolderName("IndexController"));
        assertEquals("all-jobs", ZendUtils.getViewFolderName("AllJobsController"));
    }
}
