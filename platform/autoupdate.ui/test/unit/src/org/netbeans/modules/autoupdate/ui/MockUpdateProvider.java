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

package org.netbeans.modules.autoupdate.ui;

import java.io.IOException;
import java.util.Map;
import org.junit.Assert;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MockUpdateProvider implements UpdateProvider {
    private static Map<String,UpdateItem> updateItems;
    private static Map<String,UpdateItem> pendingUpdateItems;
    
    public static void setUpdateItems(Map<String,UpdateItem> items) {
        pendingUpdateItems = items;
    }
    
    
    @Override
    public String getName() {
        return "MockUpdateProvider";
    }

    @Override
    public String getDisplayName() {
        return "Mock Update Provider";
    }

    @Override
    public String getDescription() {
        return "Sample mock update provider";
    }

    @Override
    public CATEGORY getCategory() {
        return CATEGORY.STANDARD;
    }

    @Override
    public Map<String, UpdateItem> getUpdateItems() throws IOException {
        if (updateItems == null) {
            Assert.assertNotNull("Pending items are provided", pendingUpdateItems);
            updateItems = pendingUpdateItems;
            pendingUpdateItems = null;
        }
        return updateItems;
    }

    @Override
    public boolean refresh(boolean force) throws IOException {
        Assert.assertNotNull("Pending items are provided", pendingUpdateItems);
        updateItems = null;
        return true;
    }

}
