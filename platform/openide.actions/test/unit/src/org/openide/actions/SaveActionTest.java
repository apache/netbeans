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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


public class SaveActionTest extends NbTestCase
implements SaveCookie {
    private int cnt;

    public SaveActionTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testActionWorksOnSaveCookieOnly() {
        cnt = 0;
        Lookup lkp = Lookups.singleton(this);
        SaveAction sa = SaveAction.get(SaveAction.class);
        Action clone = sa.createContextAwareInstance(lkp);
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Save was called", 1, cnt);
    }

    public void save() throws IOException {
        cnt++;
    }
    
    public void testActionWorksOnMultipleSaveCookies() {
        cnt = 0;
        SaveCookie cookie1 = new SaveCookieTestImpl();
        SaveCookie cookie2 = new SaveCookieTestImpl();
        Lookup lkp = Lookups.fixed(cookie1, cookie2);
        SaveAction sa = SaveAction.get(SaveAction.class);
        Action clone = sa.createContextAwareInstance(lkp);
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Save was called multiple times", 2, cnt);
    }

    private class SaveCookieTestImpl implements SaveCookie {

        @Override
        public void save() throws IOException {
            cnt++;
        }
    }
}
