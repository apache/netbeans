/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.junit.ui.api;

import java.awt.Color;
import org.netbeans.modules.java.testrunner.ui.api.JumpAction;
import javax.swing.Action;
import javax.swing.UIManager;
import org.netbeans.modules.gsf.testrunner.ui.api.CallstackFrameNode;

/**
 *
 * @author answer
 */
public class JUnitCallstackFrameNode extends CallstackFrameNode{
    private final String projectType;
    private final String testingFramework;

    public JUnitCallstackFrameNode(String frameInfo, String displayName, String projectType, String testingFramework) {
        super(frameInfo, displayName);
        this.projectType = projectType;
        this.testingFramework = testingFramework;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action preferred = getPreferredAction();
        if (preferred != null) {
            return new Action[] { preferred };
        }
        return new Action[0];
    }

    @Override
    public String getDisplayName() {
        String line = super.getDisplayName();
        String trimmed = line.trim();
        if (trimmed.startsWith("at ") && line.endsWith(")")) {
            return isRelevant(trimmed) ?
                    "<html>    <a href=\"\">"+line+"</a></html>" 
                  : "<html>    <font color="+hiddenColor()+">"+line+"</font></html>";
        }
        return line;
    }

    private static String hiddenColor() {
        // note: the tree adjusts the color automatically if the contrast is too low
        // which would have the opposite effect of what we are trying to achieve here
        float a = 0.6f;
        Color f = UIManager.getColor("Tree.foreground");
        Color b = UIManager.getColor("Tree.background");
        return String.format("#%02x%02x%02x", 
                (int)(b.getRed()   + a * (f.getRed()   - b.getRed())),
                (int)(b.getGreen() + a * (f.getGreen() - b.getGreen())),
                (int)(b.getBlue()  + a * (f.getBlue()  - b.getBlue())));
    }

    private boolean isRelevant(String stackFrame) {
        return !stackFrame.startsWith("at org.junit.Ass")
            && !stackFrame.startsWith("at org.junit.jupiter.api.Ass");
    }

    @Override
    public Action getPreferredAction() {
        return new JumpAction(this, frameInfo, projectType, testingFramework);
    }

}
