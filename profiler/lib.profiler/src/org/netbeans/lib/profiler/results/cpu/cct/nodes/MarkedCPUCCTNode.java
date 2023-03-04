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

package org.netbeans.lib.profiler.results.cpu.cct.nodes;

import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public class MarkedCPUCCTNode extends TimedCPUCCTNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class Locator {

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private Locator() {
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public static MarkedCPUCCTNode locate(Mark mark, RuntimeCCTNode[] nodes) {
            for(RuntimeCCTNode n : nodes) {
                if (n instanceof MarkedCPUCCTNode && ((MarkedCPUCCTNode)n).getMark().equals(mark)) {
                    return (MarkedCPUCCTNode)n;
                }
            }
            return null;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Mark mark;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of MarkedCPUCCTNode */
    public MarkedCPUCCTNode(Mark mark) {
        super();
        this.mark = mark;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Mark getMark() {
        return mark;
    }

    public boolean isRoot() {
        return false;
    }
    
    public boolean equals(Object otherNode) {
        if (otherNode == null) {
            return false;
        }

        if (!(otherNode instanceof MarkedCPUCCTNode)) {
            return false;
        }

        return mark.equals(((MarkedCPUCCTNode) otherNode).getMark());
    }

    public int hashCode() {
        return (mark == null) ? 0 : mark.hashCode();
    }

    protected TimedCPUCCTNode createSelfInstance() {
        return new MarkedCPUCCTNode(mark);
    }
}
