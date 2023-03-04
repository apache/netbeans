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

package org.netbeans.lib.profiler.ui.cpu.statistics;

import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode;
import javax.swing.JPanel;
import org.netbeans.lib.profiler.marker.Mark;


/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class StatisticalModule extends JPanel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int mId = -1;
    private Mark mark = Mark.DEFAULT;
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public final void setSelectedMethodId(int methodId) {
        int oldId = this.mId;
        this.mId = methodId;
        if (oldId != this.mId) {
            onMethodSelectionChange(oldId, this.mId);
        }
    }
        
    protected final int getSelectedMethodId() {
        return mId;
    }
    
    public final void setSelectedMark(Mark mark) {
        Mark oldMark = this.mark;
        this.mark = mark;
        
        if (!oldMark.equals(this.mark)) {
            onMarkSelectionChange(oldMark, this.mark);
        }
    }
    
    protected final Mark getSelectedMark() {
        return this.mark;
    }
    
    public abstract void refresh(RuntimeCPUCCTNode appNode);
    
    protected void onMarkSelectionChange(Mark oldMark, Mark newMark) {
    }
    
    protected void onMethodSelectionChange(int oldMethodId, int newMethodId) {
    }
}
