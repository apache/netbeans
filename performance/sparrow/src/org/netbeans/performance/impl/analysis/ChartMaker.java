/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
