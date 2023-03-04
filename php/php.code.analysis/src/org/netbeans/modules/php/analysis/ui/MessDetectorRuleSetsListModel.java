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
package org.netbeans.modules.php.analysis.ui;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import org.netbeans.modules.php.analysis.commands.MessDetector;

public final class MessDetectorRuleSetsListModel extends AbstractListModel<String> {

    private static final long serialVersionUID = -978897545211100014L;
    // @GuardedBy("EDT")
    private final List<String> ruleSets;


    public MessDetectorRuleSetsListModel() {
        assert EventQueue.isDispatchThread();
        this.ruleSets = getAllRuleSets();
    }

    public static List<String> getAllRuleSets() {
        return new ArrayList<>(MessDetector.RULE_SETS);
    }

    @Override
    public int getSize() {
        assert EventQueue.isDispatchThread();
        return ruleSets.size();
    }

    @Override
    public String getElementAt(int index) {
        assert EventQueue.isDispatchThread();
        return ruleSets.get(index);
    }

}
