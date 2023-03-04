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

package org.netbeans.lib.profiler.results.cpu;

import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.ExportDataDumper;
import java.text.NumberFormat;


/**
 * Presentation-Time CPU Profiling Calling Context Tree (CCT) Node that contains all necessary data in its data fields.
 * These objects are used for reverse CCTs and other trees that have to be constructed incrementally, so that backing by
 * flattened data cannot be easily implemented.
 *
 * @author Misha Dmitriev
 */
public class PrestimeCPUCCTNodeFree extends PrestimeCPUCCTNode {

    private static NumberFormat percentFormat=null;
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

//    protected int methodId;
//    protected int nCalls;
//    protected long sleepTime0;
//
//    /** The same class used for both standard and "extended" nodes (collecting one or two timestamps) */
//    protected long totalTime0;
//    protected long totalTime1;
//
//    /** The same class used for both standard and "extended" nodes (collecting one or two timestamps) */
//    protected long waitTime0;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Constructor for creating normal nodes representing methods
     */
    protected PrestimeCPUCCTNodeFree(CPUCCTContainer container, PrestimeCPUCCTNode parent, int methodId) {
        super(container, parent, methodId);
//        this.methodId = methodId;
    }
    
    PrestimeCPUCCTNodeFree() {}

    //~ Methods ------------------------------------------------------------------------------------------------------------------

//    public CCTNode getChild(int index) {
//        return children[index];
//    }
//
//    public CCTNode[] getChildren() {
//        return children;
//    }
    
    PrestimeCPUCCTNode createCopy() {
        PrestimeCPUCCTNodeFree copy = new PrestimeCPUCCTNodeFree();
        setupCopy(copy);
        return copy;
    }
    
    public CCTNode createFilteredNode() {
        PrestimeCPUCCTNodeFree filtered = new PrestimeCPUCCTNodeFree();
        setupFilteredNode(filtered);
        return filtered;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

//    public int getMethodId() {
//        return methodId;
//    }

//    public int getNCalls() {
//        return nCalls;
//    }

//    public int getNChildren() {
//        return (children != null) ? children.length : 0;
//    }

//    public long getSleepTime0() {
//        return 0; // TODO [wait]
//    }

//    public int getThreadId() {
//        return container.getThreadId();
//    }

//    public long getTotalTime0() {
//        return totalTime0;
//    }

    public float getTotalTime0InPerCent() {
        float result = (float) ((double) totalTime0 / container.getWholeGraphNetTime0() * 100);

        return (result < 100) ? result : 100f;
    }

//    public long getTotalTime1() {
//        return totalTime1;
//    }

    public float getTotalTime1InPerCent() {
        return (float) ((double) totalTime1 / container.getWholeGraphNetTime1() * 100);
    }

//    public long getWaitTime0() {
//        return 0; // TODO [wait]
//    }
    
//    void merge(PrestimeCPUCCTNodeFree node) {
//        addNCalls(node.getNCalls());
//        addSleepTime0(node.getSleepTime0());
//        addTotalTime0(node.getTotalTime0());
//        addTotalTime1(node.getTotalTime1());
//        addWaitTime0(node.getWaitTime0());
//        
//        if (node.children != null) {
//            for (PrestimeCPUCCTNode ch : node.children)
//                ch.parent = this;
//            
//            int chl = children == null ? 0 : children.length;
//            int newchl = node.children.length;
//            PrestimeCPUCCTNodeFree[] newch = new PrestimeCPUCCTNodeFree[chl + newchl];
//            if (children != null) System.arraycopy(children, 0, newch, 0, chl);
//            System.arraycopy(node.children, 0, newch, chl, newchl);
//            children = newch;
//        }
//    }

    public void addChild(PrestimeCPUCCTNodeFree node) {
        if (children == null) {
            children = new PrestimeCPUCCTNodeFree[1];
        } else {
            PrestimeCPUCCTNodeFree[] newch = new PrestimeCPUCCTNodeFree[children.length + 1];
            System.arraycopy(children, 0, newch, 0, children.length);
            children = newch;
        }

        children[children.length - 1] = node;
    }

//    /**
//     * Methods used during node merging
//     */
//    public void addNCalls(int addCalls) {
//        nCalls += addCalls;
//    }
//
//    public void addSleepTime0(long addTime) {
//        sleepTime0 += addTime;
//    }
//
//    public void addTotalTime0(long addTime) {
//        totalTime0 += addTime;
//    }
//
//    public void addTotalTime1(long addTime) {
//        totalTime1 += addTime;
//    }
//
//    public void addWaitTime0(long addTime) {
//        waitTime0 += addTime;
//    }

    /**
     * Create a copy of this node, but with zero children
     */
    public PrestimeCPUCCTNodeFree createChildlessCopy() {
        try {
            PrestimeCPUCCTNodeFree res = (PrestimeCPUCCTNodeFree) this.clone();
            res.children = null;

            return res;
        } catch (CloneNotSupportedException ex) {
            return null; /* Shouldn't happen */
        }
    }
    
    public void exportXMLData(ExportDataDumper eDD,String indent) {
        String newline = System.getProperty("line.separator"); // NOI18N
        StringBuffer result = new StringBuffer(indent+"<node>"+newline); //NOI18N
        result.append(indent).append(" <Name>").append(replaceHTMLCharacters(getNodeName())).append("</Name>").append(newline); //NOI18N
        CCTNode p = getParent();
        result.append(indent).append(" <Parent>").append(replaceHTMLCharacters((p==null)?("none"):(((PrestimeCPUCCTNodeFree)getParent()).getNodeName()))).append("</Parent>").append(newline); //NOI18N
        result.append(indent).append(" <Time_Relative>").append(percentFormat.format( p==null ? ((double)getTotalTime0InPerCent())/100 : 100 )).append("</Time_Relative>").append(newline); //NOI18N
        result.append(indent).append(" <Time>").append(p==null ? getTotalTime0() : "N/A").append("</Time>").append(newline); //NOI18N
        if (container.collectingTwoTimeStamps) {
            result.append(indent).append(" <Time-CPU>").append(getTotalTime1()).append("</Time-CPU>").append(newline); //NOI18N
        }
        result.append(indent).append(" <Invocations>").append(p==null ? getNCalls() : 1 ).append("</Invocations>").append(newline); //NOI18N
        eDD.dumpData(result); //dumps the current row
        // children nodes
        if (children!=null) {
            for (int i = 0; i < getNChildren(); i++) {
                ((PrestimeCPUCCTNodeFree)children[i]).exportXMLData(eDD, indent+"  "); //NOI18N
            }
        }
        result=new StringBuffer(indent+"</node>"); //NOI18N
        eDD.dumpData(result);
    }

    public void exportHTMLData(ExportDataDumper eDD, int depth) {
        StringBuffer result = new StringBuffer("<tr><td class=\"method\"><pre class=\"method\">"); //NOI18N
        for (int i=0; i<depth; i++) {
            result.append("."); //NOI18N
        }
        result.append(replaceHTMLCharacters(getNodeName())).append("</pre></td><td class=\"right\">").append(percentFormat.format(((double)getTotalTime0InPerCent())/100)).append("</td><td class=\"right\">").append(getTotalTime0());
        if (container.collectingTwoTimeStamps) {
            result.append("</td><td class=\"right\">").append(getTotalTime1());
        }
        result.append("</td><td class=\"right\">").append(getNCalls()).append("</td></tr>"); //NOI18N
        eDD.dumpData(result); //dumps the current row
        // children nodes
        if (children!=null) {
            for (int i = 0; i < getNChildren(); i++) {
                ((PrestimeCPUCCTNodeFree)children[i]).exportHTMLData(eDD, depth+1);
            }
        }
    }

    private String replaceHTMLCharacters(String s) {
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; i++) {
          char c = s.charAt(i);
          switch (c) {
              case '<': sb.append("&lt;"); break; // NOI18N
              case '>': sb.append("&gt;"); break; // NOI18N
              case '&': sb.append("&amp;"); break; // NOI18N
              case '"': sb.append("&quot;"); break; // NOI18N
              default: sb.append(c); break;
          }
        }
        return sb.toString();
    }

    public void exportCSVData(String separator, int depth, ExportDataDumper eDD) {
        StringBuffer result = new StringBuffer();
        String newLine = "\r\n"; // NOI18N
        String quote = "\""; // NOI18N
        String indent = " "; // NOI18N

        // this node
        result.append(quote);
        for (int i=0; i<depth; i++) {
            result.append(indent); // to simulate the tree structure in CSV
        }
        result.append(getNodeName()).append(quote).append(separator);
        result.append(quote).append(getTotalTime0InPerCent()).append(quote).append(separator);
        result.append(quote).append(getTotalTime0()).append(quote).append(separator);
        if (container.collectingTwoTimeStamps) {
            result.append(quote).append(getTotalTime1()).append(quote).append(separator);
        }
        result.append(quote).append(getNCalls()).append(quote).append(newLine);
        eDD.dumpData(result); //dumps the current row
        // children nodes
        if (children!=null) {
            for (int i = 0; i < getNChildren(); i++) {
                ((PrestimeCPUCCTNodeFree)children[i]).exportCSVData(separator, depth+1, eDD);
            }
        }
    }

    public static void setPercentFormat(NumberFormat percentFormat) {
        PrestimeCPUCCTNodeFree.percentFormat = percentFormat;
    }
}
