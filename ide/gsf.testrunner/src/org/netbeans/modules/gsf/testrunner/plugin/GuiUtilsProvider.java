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
package org.netbeans.modules.gsf.testrunner.plugin;

import java.awt.Color;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Theofanis Oikonomou
 */
public abstract class GuiUtilsProvider {
    
    public abstract String getMessageFor(String key);
    public abstract ResourceBundle getBundle();
    public abstract JTextComponent createMultilineLabel(String text, Color color);
    public abstract String getCheckboxText(String key);
    public abstract JCheckBox[] createCheckBoxes(String[] ids);
    public abstract JComponent createChkBoxGroup(String title, JCheckBox[] elements);
    public abstract String getTestngFramework();
    public abstract String getJunitFramework();
    
}
