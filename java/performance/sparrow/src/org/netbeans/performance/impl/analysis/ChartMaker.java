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
/*
 * ChartMaker.java
 *
 * Created on October 16, 2002, 10:42 PM
 */

package org.netbeans.performance.impl.analysis;
import org.netbeans.performance.spi.*;
import org.netbeans.performance.impl.chart.*;
import java.util.*;
//import org.netbeans.performance.impl.logparsing.*;
import java.io.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import com.jrefinery.data.*;
import com.jrefinery.chart.*;

/**An ant task to generate some charts.
 *
 * @author  Tim Boudreau
 */
public class ChartMaker extends Analyzer {

    /** Creates a new instance of ChartMaker */
    public ChartMaker() {

    }

    public String analyze() throws BuildException {
        try {
            DataAggregation data = getData();
            genCharts ();
            return "Wrote charts to " + outdir;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException (e.getMessage(), e);
        }
    }

    String query = null;
    public void setQuery (String query) {
        this.query = query;
    }
    
    private String[] elements = new String[]{};
    public void setElements (String elements) {
        StringTokenizer sk = new StringTokenizer (elements, ",");
        ArrayList al = new ArrayList (elements.length() / 5);
        while (sk.hasMoreTokens()) {
            al.add (sk.nextToken().trim());
        }
        this.elements = new String[al.size()];
        this.elements = (String[]) al.toArray(this.elements);
    }
    
    
    private String xtitle="X Axis";
    private String ytitle="Y Axis";
    String title = "Chart";
    
    public void setXTitle (String s) {
        xtitle=s;
    }
    
    public void setYTitle (String s) {
        ytitle=s;
    }
    
    public void setTitle (String s) {
        title=s;
    }
    
    public void genCharts () throws BuildException {
        if (query == null) throw new BuildException ("No query specified for " + title);
        if (elements.length == 0) throw new BuildException ("No elements specified");
        try {
            NbStatisticalDataset nsd = new NbStatisticalDataset (query, elements, getData());
            NbChart chart = new NbChart (title, xtitle, ytitle, nsd);
            File f;
            if (outfile == null) {
                f = new File (outdir + "/" + title);
            } else {
                f = new File (outfile);
            }
            if (f.exists()) 
                System.out.println("Overwriting " + f);
            ChartUtilities.saveChartAsPNG(f, chart, nsd.getOptimalWidth(), 680);
        } catch (Exception e) {
            throw new BuildException (e);
        }
    }
    
    public static void main (String[] args) throws Exception {
        ChartMaker cm = new ChartMaker();
        cm.setElements (
            "Seconds spent in GC,Seconds in MAJOR GC cycles,Seconds in minor GC cycles"
            );
        cm.setQuery ("/run/GC Tuning*gcinfo");
        cm.setOutFile("/tmp/testchart.png");
        cm.setDataFile("/tmp/SerThing.ser");
        cm.execute();
        System.out.println("Wrote chart to /tmp/testchart.png");
    }
    
}
