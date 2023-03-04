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

package org.netbeans.modules.ide.ergonomics;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;

/** Special ergonomics warm up extension that listens on changes in set of
 * enabled features and re-runs the warm up to make newly added features
 * ready for use.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class WarmUp implements ChangeListener {
    private WarmUp() {
    }
    
    static void init() {
        FeatureManager.getInstance().addChangeListener(new WarmUp());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        MainLookup.warmUp(5000);
    }
}
