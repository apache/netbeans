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

package org.netbeans.test.junit.utils;

import java.awt.Component;
import javax.swing.JComponent;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JComponentOperator;

/**
 * This operates StatisticsPanel from inside JUnit Tests results window
 * @author Max Sauer
 */
public class StatisticsPanelOperator extends JComponentOperator {
    
    /** 
     * Waits for index-th output tab with given name.
     * It is activated by defalt.
     * @param name name of output tab to look for
     * @param index index of requested output tab with given name
     */
    public StatisticsPanelOperator() {
        super((JComponent) new ResultWindowOperator().waitSubComponent(statisticsSubchooser));
    }
    
    private static final ComponentChooser statisticsSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("StatisticsPanel"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.modules.junit.output.StatisticsPanel";// NOI18N
        }
    };
    
}
