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

package org.netbeans.api.debugger.providers;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.debugger.ui.ColumnModelRegistration;
import org.netbeans.spi.viewmodel.ColumnModel;

/**
 *
 * @author Martin Entlicher
 */
@ColumnModelRegistration(path="unittest/annotated")
public class TestColumnModel extends ColumnModel{

    public static final String ID = "TestID";
    public static final String DisplayName = "Test Display Name";
    public static final Class TYPE = java.util.LinkedHashMap.class;

    public static Set<TestColumnModel> INSTANCES = new HashSet<TestColumnModel>();

    public TestColumnModel() {
        INSTANCES.add(this);
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DisplayName;
    }

    @Override
    public Class getType() {
        return TYPE;
    }

}
