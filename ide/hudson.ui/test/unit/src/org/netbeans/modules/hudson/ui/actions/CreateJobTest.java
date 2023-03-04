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
package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;

/**
 *
 * @author jhavlin
 */
public class CreateJobTest {

    /**
     * Test for bug 230434 - NullPointerException at
     * org.netbeans.modules.hudson.ui.actions.CreateJob.actionPerformed.
     */
    @Test
    public void testRunCustomActionIfAvailable() {
        CreateJob globalAction = (CreateJob) CreateJob.global();
        assertFalse(globalAction.runCustomActionIfAvailable(null));

        HudsonInstance hi = HudsonManager.addInstance(
                "x230434", "http://x230434/", 1,
                HudsonInstance.Persistence.tranzient("some info",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                    }
                }));
        CreateJob withCustomAction = new CreateJob(hi);
        assertTrue(withCustomAction.runCustomActionIfAvailable(null));
        HudsonManager.removeInstance(hi);
    }
    
}
