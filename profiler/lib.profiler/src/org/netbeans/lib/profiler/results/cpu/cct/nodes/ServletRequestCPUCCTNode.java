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

import org.netbeans.lib.profiler.results.RuntimeCCTNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public class ServletRequestCPUCCTNode extends TimedCPUCCTNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class Locator {

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private Locator() {
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public static ServletRequestCPUCCTNode locate(int requestType, String servletPath, RuntimeCCTNode[] nodes) {
            for(RuntimeCCTNode n : nodes) {
                if (n instanceof ServletRequestCPUCCTNode) {
                    ServletRequestCPUCCTNode sn = (ServletRequestCPUCCTNode)n;
                    if (sn.getServletPath().equals(servletPath) && sn.getRequestType() == requestType) {
                        return sn;
                    }
                }
            }
            return null;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    int hashCode = 0;
    private final String servletPath;
    private final int requestType;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of ServletRequestCPUCCTNode
     */
    public ServletRequestCPUCCTNode(int requestType, String path) {
        super();
        this.servletPath = path;
        this.requestType = requestType;
        setFilteredStatus(FILTERED_YES); // boundary node is going to be filtered by default
        setNCalls(0);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getRequestType() {
        return requestType;
    }

    public boolean isRoot() {
        return false;
    }

    public String getServletPath() {
        return servletPath;
    }

    public boolean equals(Object otherNode) {
        if (otherNode == null) {
            return false;
        }

        if (!(otherNode instanceof ServletRequestCPUCCTNode)) {
            return false;
        }

        return servletPath.equals(((ServletRequestCPUCCTNode) otherNode).servletPath)
               && (requestType == ((ServletRequestCPUCCTNode) otherNode).requestType);
    }

    public int hashCode() {
        if (hashCode == 0) {
            hashCode = servletPath.hashCode() + (requestType * 18321);
        }

        return hashCode;
    }

    protected TimedCPUCCTNode createSelfInstance() {
        return new ServletRequestCPUCCTNode(requestType, servletPath);
    }
}
