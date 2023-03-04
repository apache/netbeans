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
 * GcCharts.java
 *
 * Created on October 9, 2002, 10:41 PM
 */

package org.netbeans.performance.impl.analysis;
import com.jrefinery.data.*;
import com.jrefinery.chart.*;
import java.util.*;
import org.netbeans.performance.spi.*;
import org.netbeans.performance.spi.html.*;
import org.netbeans.performance.impl.logparsing.*;
import java.io.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.awt.Color;
import org.netbeans.performance.impl.logparsing.TimeLog;
import org.netbeans.performance.impl.logparsing.GcLog;
/** Ant task to build basic garbage collection statistic charts.
 * Note, this class is a rather ugly, hacked together thing.
 * Once ChartMaker/NbChart/NbStatisticalDataset is in good
 * enough shape, this class should be deleted and charts and
 * analysis should be defined using XML.
 *
 * @author  Tim Boudreau
 */
public class GcCharts extends Analyzer {

    public String analyze() throws BuildException {
        try {
            DataAggregation data = getData();
            genGcCountChart();
            return "Wrote charts to " + outdir;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException (e.getMessage(), e);
        }
    }    
    
    public static final ElementFilter XmsFilter = new LineswitchFilter ("ms");
    public static final ElementFilter XmxFilter = new LineswitchFilter ("mx");
    public static final ElementFilter NewSizeFilter = new LineswitchFilter ("NewSize");
    public static final ElementFilter PermSizeFilter = new LineswitchFilter ("mx");
    public static final ElementFilter SurvivorRatioSizeFilter = new LineswitchFilter ("TargetSurvivorRatio");
    
    
    public static final ElementFilter AllLineswitchesFilter = new ClassFilter (JavaLineswitch.class);
    public static final ElementFilter ModulesFilter = new ClassFilter (ModuleEntry.class);

    public static final ElementFilter GcCountFilter = new NameElementFilter (GcLog.GC_TOTAL);
    public static final ElementFilter secondsInGcComparator = new NameElementFilter (GcLog.GC_SECONDS);
    public static final Comparator gcCountComparator  = new FindValueComparator (GcCountFilter);    
    public static final Comparator xmxcomparator = new FindValueComparator (XmxFilter);    
    public static final Comparator xmscomparator = new FindValueComparator (XmsFilter);    
    
    
    static class NameElementFilter implements ElementFilter {
        String name;
        
        public NameElementFilter (String name) {
            this.name = name;
        }
        
        public boolean accept(LogElement le) {
            boolean result = (le instanceof Named);
            if (result) result =((Named) le).getName().equals(name);
            return result;
        }
        
    }

    
    
    /**Comparator that simplifies the work of finding a particular item
     * in a DataAggregation.  The compareTo() method is passed two 
     * instances of DataAggregation.  The ElementFilter passed to the
     * constructor is used to find the appropriate children of each
     * aggregation.  Note the filter must match only <i>one</i>
     * element in each aggregation. */
    static class FindValueComparator implements Comparator {
        ElementFilter el;
        
        public FindValueComparator(ElementFilter el) {
            this.el = el;
        }

        public int compare(Object o1, Object o2) {
            if ((o1 instanceof DataAggregation) && (o2 instanceof DataAggregation)) {
                DataAggregation da1, da2;
                da1 = (DataAggregation) o1;
                da2 = (DataAggregation) o2;
                List la = da1.findElements (el);
                List lb = da2.findElements (el);
                if ((la.size() > 1) || (lb.size() > 1)) {
                    throw new IllegalArgumentException ("More than one matching object");
                }
                if (!((((Valued) la.get(0)).getValue() instanceof Comparable) && (((Valued) la.get(0)).getValue() instanceof Comparable))) {
                    throw new IllegalArgumentException ("Objects returned by filter must implement Comparable.  " + ((Valued) la.get(0)).getValue() + " and " + ((Valued) lb.get(0)).getValue() + " do not.");
                }
                Comparable val1 = (Comparable) ((Valued) la.get(0)).getValue();
                Comparable val2 = (Comparable) ((Valued) lb.get(0)).getValue();
                return val2.compareTo(val1);
            } else {
                throw new IllegalArgumentException ("FindValueComparator can only compare instances of DataAggregation, not " + o1 + " and " + o2);
            }
        }
        
    }
    
    static class ClassFilter implements ElementFilter {
        Class c;
        public ClassFilter (Class c) {
            this.c = c;
        }
        
        public boolean accept(LogElement le) {
            return c.isAssignableFrom(le.getClass());
        }
        
    }
    
    static class LineswitchFilter implements ElementFilter {
        String id;
        public LineswitchFilter (String identifier) {
            id = identifier;
        }
        
        public boolean accept(LogElement le) {
            boolean result = le instanceof JavaLineswitch;
            if (result) {
                result = ((JavaLineswitch) le).getIdentifier().equals(id);
            }
            return result;
        }
    }    

    Number fetchSdv (DataAggregation gcinfo, String key) {
        try {
            AveragedNameValueLogElement el = (AveragedNameValueLogElement) gcinfo.findChild(key);
            Number result = el.getStandardDeviation();
            if (result.floatValue()==Float.NaN) result = new Float (0);
            return result;
        } catch (Exception e) {
            System.out.println("NPE looking for |" + key + "| in " + gcinfo);
            throw new RuntimeException (e);            
        }
    }
    
    int htmlwriterindex = -1;
    Number fetchGcProp (DataAggregation gcinfo, String key) {
        try {
        ValueLogElement el = (ValueLogElement) gcinfo.findChild (key);
//            System.out.println(key + "=" + el.getValue());
            Number result = (Number) el.getValue();
            if (result.floatValue()==Float.NaN) result = new Float (0);
            return result;
        } catch (NullPointerException npe) {
            System.out.println("NPE looking for |" + key + "| in " + gcinfo);
            throw npe;
        }
    }
    
    private LogElement[] filterCfgs (LogElement[] el, boolean eliminateAllWithMajorGC, final HTMLContainer report) {
        ArrayList al = new ArrayList(el.length);
        al.addAll(Arrays.asList(el));
        DataAggregation curr;
        DataAggregation gcinfo;
        DataAggregation bestByTime=null;
        DataAggregation bestByGcCount=null;
        DataAggregation bestByAvgGcDuration=null;
        DataAggregation secondBestByTime=null;
        DataAggregation secondBestByGcCount=null;
        DataAggregation secondBestByAvgGcDuration=null;
        int bestSessionTime = Integer.MAX_VALUE;
        int bestGcCount = Integer.MAX_VALUE;
        int bestDuration = Integer.MAX_VALUE;

        int secondBestSessionTime = Integer.MAX_VALUE;
        int secondBestGcCount = Integer.MAX_VALUE;
        int secondBestDuration = Integer.MAX_VALUE;
        
        HTMLUnorderedList elims = new HTMLUnorderedList("Settings eliminated because they caused major garbage collections during startup", HTML.SINGLE_ROW);
        
        boolean noneRemoved = true;
        for (Iterator i=al.iterator();i.hasNext();) {
            curr=(DataAggregation) i.next();
            gcinfo = (DataAggregation) curr.getParent().findChild ("gcinfo");
            //first, remove all entries containing full gc cycles for startup
            //NOTE, REMOVE THIS FILTER FOR UI TESTS!
            Number num = (Number) fetchGcProp (gcinfo, "Number of full GC cycles");
            if ((num.intValue() != 0) && (eliminateAllWithMajorGC)) {
                if (!(curr.getParent().getName().equals("baseline"))) {
                    i.remove ();
                    elims.add (new HTMLListItem (curr.getParent().getName(), getJVMSwitches (curr, false)));
                    noneRemoved=false;
                }
            } else {
                Number n = (Number) fetchGcProp ((DataAggregation) curr.getParent().findChild("timelog"), TimeLog.DURATION);
                if (n.intValue() < bestSessionTime) {
                    secondBestSessionTime = bestSessionTime;
                    bestSessionTime = n.intValue();
                    secondBestByTime = bestByTime;
                    bestByTime=curr;
                }
                n = (Number) fetchGcProp (gcinfo, "Number of GC cycles");
                if (n.intValue() < bestGcCount) {
                    secondBestGcCount = bestGcCount;
                    bestGcCount = n.intValue();
                    secondBestByGcCount = bestByGcCount;
                    bestByGcCount=curr;
                }
                String key = eliminateAllWithMajorGC ? "Average milliseconds per minor GC" : "Average milliseconds per MAJOR GC";
                n = (Number) fetchGcProp (gcinfo, key);
                if (n.intValue() < bestDuration) {
                    secondBestDuration = bestDuration;
                    bestDuration = n.intValue();
                    secondBestByAvgGcDuration = bestByAvgGcDuration;
                    bestByAvgGcDuration=curr;
                }
            }
        }
        if (!noneRemoved) report.add (elims);
        HTMLUnorderedList wins = new HTMLUnorderedList ("Most effective settings", HTML.SINGLE_ROW);
        report.add (wins);
        wins.add (new HTMLListItem ("Lowest session time", getJVMSwitches (bestByTime, false), "#" + bestByTime.getParent().getName()));
        wins.add (new HTMLListItem ("Lowest gc count",  getJVMSwitches (bestByGcCount, false), "#" + bestByGcCount.getParent().getName()));
        wins.add (new HTMLListItem ("Lowest by average gc duration", "(by " + (eliminateAllWithMajorGC ? " minor " : " major ") + "gc duration) " + bestByAvgGcDuration.getParent().getName() + getJVMSwitches (bestByAvgGcDuration, false), "#" + bestByAvgGcDuration.getParent().getName()));

        LogElement[] result = new LogElement[al.size()];
        result = (LogElement[]) al.toArray (result);
        
        Comparator c = new Comparator () {
            public int compare (Object a, Object b) {
                DataAggregation x = (DataAggregation) a;
                DataAggregation y = (DataAggregation) b;
                Float c = (Float) ((ValueLogElement) ((DataAggregation) x.getParent().findChild("timelog")).findChild (TimeLog.DURATION)).getValue();
                Float d = (Float) ((ValueLogElement) ((DataAggregation) y.getParent().findChild("timelog")).findChild (TimeLog.DURATION)).getValue();
                return c.compareTo(d);
            }
        };
        Arrays.sort (result, c);
        return result;
    }
    
    private void genGcCountChart () throws BuildException {
        DataAggregation data;
        try {
            data = getData();
        } catch (Exception e) {
            throw new BuildException ("Could not retrieve data", e);
        }
        String queryString = "/run/GC Tuning*javaconfig";
        LogElement[] cfgs = data.query (queryString);
        
        HTMLSubdocument summary = new HTMLSubdocument();
        cfgs = filterCfgs(cfgs,true,summary);
        
        int runcount = cfgs.length; 
        if (runcount == 0) throw new DataNotFoundException ("Query returned no results.  No results to build charts for! Query was: " + queryString);
        Number[][] gcSecondsData = new Number[3][runcount];
        Number[][] gcSecondsDevData = new Number[3][runcount];
        String[] gcSecondsFields = new String [] {
            "Seconds spent in GC", 
            "Seconds in MAJOR GC cycles",
            "Seconds in minor GC cycles"
        };

        
        Number[][] gcCountData = new Number[3][runcount];
        Number[][] gcCountDevData = new Number[3][runcount];
        String[] gcCountFields = new String [] {
            "Number of GC cycles",
            "Number of minor GC cycles",
            "Number of full GC cycles"
        };

        Number [][] gcAverageTimeData = new Number[2][runcount];
        Number[][] gcAverageTimeDevData = new Number[2][runcount];
        String[] gcAverageTimeFields = new String [] {
            "Average milliseconds per minor GC",
            "Average milliseconds per MAJOR GC"
        };
        
        Number[][] sessionLengthData = new Number[1][runcount];
        Number[][] sessionLengthDevData = new Number[1][runcount];
        String[] sessionLengthFields = new String [] {
            TimeLog.DURATION
        };

        String[] categories = new String[runcount];

        DataAggregation currConfig;
        DataAggregation sysinfo;
        DataAggregation gcinfo;
        TimeLog timelog;
        String cfginfo = ((ValueLogElement) ((DataAggregation) cfgs[0].getParent().findChild("sysinfo")).findChild("Operating System")).getValue().toString();
        HTMLDocument doc = createReport(cfginfo);
        doc.add (summary);
        HTMLSubdocument detail = new HTMLSubdocument();
        HTMLTable currSection;
        HTMLSubdocument sectDoc;
        HTMLUnorderedList index = new HTMLUnorderedList("Index of all settings tested (sorted by session duration)");
        doc.add (index);
        doc.add (detail);
        String configname;
        
        for (int i=0; i < runcount; i++) {
            currConfig = (DataAggregation) cfgs[i];
            configname = currConfig.getParent().getName();
            currSection = new HTMLTable (configname, 3);
            sectDoc = new HTMLSubdocument (configname, configname);
            sectDoc.add (currSection);
            detail.add (sectDoc);
            
            sysinfo = (DataAggregation) currConfig.getParent().findChild ("sysinfo");
            gcinfo = (DataAggregation) currConfig.getParent().findChild ("gcinfo");
            timelog = (TimeLog) currConfig.getParent().findChild ("timelog");
            
            //add a linked list item to the index
            index.add (new HTMLListItem (configname, getJVMSwitches (currConfig, false), "#" + configname));
            currSection.add (gcinfo.toHTML());
            currSection.add (timelog.toHTML());
            currSection.add (currConfig.toHTML());
//            categories[i] = getJVMSwitches (currConfig, true);
            categories[i] = configname;
            
            gcSecondsData[0][i] = fetchGcProp (gcinfo, gcSecondsFields[0]);
            gcSecondsDevData [0][i] = fetchSdv (gcinfo, gcSecondsFields[0]);
            gcSecondsData[1][i] = fetchGcProp (gcinfo, gcSecondsFields[1]);
            gcSecondsDevData [1][i] = fetchSdv (gcinfo, gcSecondsFields[1]);
            gcSecondsData[2][i] = fetchGcProp (gcinfo, gcSecondsFields[2]);
            gcSecondsDevData [2][i] = fetchSdv (gcinfo, gcSecondsFields[2]);
            
            gcCountData [0][i] = fetchGcProp (gcinfo, gcCountFields[0]);
            gcCountDevData [0][i] = fetchSdv (gcinfo, gcCountFields[0]);
            gcCountData [1][i] = fetchGcProp (gcinfo, gcCountFields[1]);
            gcCountDevData [1][i] = fetchSdv (gcinfo, gcCountFields[1]);
            gcCountData [2][i] = fetchGcProp (gcinfo, gcCountFields[2]);
            gcCountDevData [2][i] = fetchSdv (gcinfo, gcCountFields[2]);
            
            gcAverageTimeData[0][i] = fetchGcProp (gcinfo, gcAverageTimeFields[0]);
            gcAverageTimeDevData [0][i] = fetchSdv (gcinfo, gcAverageTimeFields[0]);

            gcAverageTimeData[1][i] = fetchGcProp (gcinfo, gcAverageTimeFields[1]);
            gcAverageTimeDevData [1][i] = fetchSdv (gcinfo, gcAverageTimeFields[1]);

            //get rid of fractional milliseconds
            gcAverageTimeData[0][i] = new Long (Math.round(gcAverageTimeData[0][i].doubleValue()));
            gcAverageTimeData[1][i] = new Long (Math.round(gcAverageTimeData[1][i].doubleValue()));
            
            sessionLengthData [0][i] = fetchGcProp (timelog, sessionLengthFields[0]);
            sessionLengthDevData [0][i] = fetchSdv (timelog, sessionLengthFields[0]);
        }

        //old code for non-statistical bar charts below
        /*
        DefaultCategoryDataset gcSecondsDataset = new DefaultCategoryDataset(gcSecondsFields, categories, gcSecondsData);
        DefaultCategoryDataset gcCountDataset = new DefaultCategoryDataset(gcCountFields, categories, gcCountData);
        DefaultCategoryDataset gcAverageTimeDataset = new DefaultCategoryDataset(gcAverageTimeFields, categories, gcAverageTimeData);
        DefaultCategoryDataset sessionLengthDataset = new DefaultCategoryDataset(sessionLengthFields, categories, sessionLengthData);
        String vmset = "VM settings";
        JFreeChart gcSecondsChart = ChartFactory.createHorizontalBarChart
            ("Seconds spent in GC", vmset, "seconds", gcSecondsDataset, true);
        
        JFreeChart gcCountChart = ChartFactory.createHorizontalBarChart
            ("Number of GC events during session", vmset, "GC event count", gcCountDataset, true);
        
        JFreeChart gcAverageTimeChart = ChartFactory.createHorizontalBarChart
            ("Average GC durations during session (milliseconds)", vmset, "average milliseconds per gc", gcAverageTimeDataset, true);
        
        JFreeChart sessionLengthChart = ChartFactory.createHorizontalBarChart
            ("Session length (milliseconds)", vmset, "milliseconds", sessionLengthDataset, false);
         */
        DefaultStatisticalCategoryDataset gcSecondsDataset = new DefaultStatisticalCategoryDataset(gcSecondsFields, categories, gcSecondsData, gcSecondsDevData);
        DefaultStatisticalCategoryDataset gcCountDataset = new DefaultStatisticalCategoryDataset(gcCountFields, categories, gcCountData, gcCountDevData);
        DefaultStatisticalCategoryDataset gcAverageTimeDataset = new DefaultStatisticalCategoryDataset(gcAverageTimeFields, categories, gcAverageTimeData, gcAverageTimeDevData);
        DefaultStatisticalCategoryDataset sessionLengthDataset = new DefaultStatisticalCategoryDataset(sessionLengthFields, categories, sessionLengthData, sessionLengthDevData);
        
        java.awt.Font titleFont = new java.awt.Font("Helvetica", java.awt.Font.BOLD, 14); 
        String vmset = "VM settings";
        CategoryItemRenderer renderer = new VerticalStatisticalBarRenderer();
        CategoryAxis xAxis = new HorizontalCategoryAxis(vmset);
        ValueAxis yAxis = new VerticalNumberAxis("seconds");          
        VerticalCategoryPlot plot = new VerticalCategoryPlot(gcSecondsDataset, xAxis, yAxis, renderer);
        JFreeChart gcSecondsChart = new JFreeChart("Seconds spent in GC", titleFont, plot, true);

        renderer = new VerticalStatisticalBarRenderer();
        xAxis = new HorizontalCategoryAxis(vmset);
        yAxis = new VerticalNumberAxis("gc event count");          
        plot = new VerticalCategoryPlot(gcCountDataset, xAxis, yAxis, renderer);
        JFreeChart gcCountChart = new JFreeChart("Number of GC events", titleFont, plot, true);
        plot.setSeriesPaint(new java.awt.Paint[] { new Color (255, 221, 15), new Color (255,149,66), new Color (149,66,255),
                                                  Color.yellow, Color.orange, Color.cyan,
                                                  Color.magenta, Color.blue});        
        
        renderer = new VerticalStatisticalBarRenderer();
        xAxis = new HorizontalCategoryAxis(vmset);
        yAxis = new VerticalNumberAxis("average milliseconds per gc");          
        plot = new VerticalCategoryPlot(gcAverageTimeDataset, xAxis, yAxis, renderer);
        JFreeChart gcAverageTimeChart = new JFreeChart("Average gc cycle duration", titleFont, plot, true);
        plot.setSeriesPaint(new java.awt.Paint[] { new Color (255, 221, 15), new Color (255,149,66), new Color (149,66,255),
                                                  Color.yellow, Color.orange, Color.cyan,
                                                  Color.magenta, Color.blue});        
        
        renderer = new VerticalStatisticalBarRenderer();
        xAxis = new HorizontalCategoryAxis(vmset);
        yAxis = new VerticalNumberAxis("session length in milliseconds");          
        plot = new VerticalCategoryPlot(sessionLengthDataset, xAxis, yAxis, renderer);
        JFreeChart sessionLengthChart = new JFreeChart("Session length", titleFont, plot, true);
        
        
        plot.setSeriesPaint(new java.awt.Paint[] { new Color (255, 221, 15), new Color (255,149,66), new Color (149,66,255),
                                                  Color.yellow, Color.orange, Color.cyan,
                                                  Color.magenta, Color.blue});        
                                                  
        
        
        customizeChart (gcSecondsChart);
        customizeChart (gcCountChart);
        customizeChart (gcAverageTimeChart);
        customizeChart (sessionLengthChart);
        
        File f1 = new File (outdir + "/gcseconds.png");
        File f2 = new File (outdir + "/gccount.png");
        File f3 = new File (outdir + "/gcavgtime.png");
        File f4 = new File (outdir + "/sessionlength.png");

        int chartWidth = Math.max (400, runcount * 60);
        int sessChartWidth = Math.max (800, runcount * 20); 
        
        try {
            ChartUtilities.saveChartAsPNG(f1, gcSecondsChart, chartWidth, 680);
            ChartUtilities.saveChartAsPNG(f2, gcCountChart, chartWidth, 680);
            ChartUtilities.saveChartAsPNG(f3, gcAverageTimeChart, chartWidth, 680);
            ChartUtilities.saveChartAsPNG(f4, sessionLengthChart, sessChartWidth, 680);
            doc.writeToFile(outdir + "/index.html");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException (e.toString(),e);
        }
    }
    
    private void customizeChart (JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        
        plot.setCategoryGapsPercent(0.3d);
        plot.setVerticalLabels(true);
        plot.setLabelFont(new java.awt.Font ("dialog", 0, 8));
        
//        HorizontalBarRenderer renderer = (HorizontalBarRenderer)chart.getCategoryPlot().getRenderer();

    }
    
    private String getJVMSwitches(DataAggregation sw, boolean truncate) {
        List l = sw.getAllElements();
        StringBuffer out = new StringBuffer (48);
        out.append (sw.getParent().getName());
        out.append (": ");
        boolean go=true;
        for (Iterator i = l.iterator(); i.hasNext() && go;) {
            out.append ((JavaLineswitch) i.next()).toString();
            if ((out.length() > 40) && truncate) {
                out.append ("...");
                go=false;
            } else {
                out.append (" ");
            }
        }
        return out.toString();
    }
    
    private HTMLDocument createReport(String sysinfo) {
        HTMLDocument doc = new HTMLDocument ("NetBeans GC performance on " + sysinfo);
        doc.add  ("The graphs below show the effect of various java lineswitch settings on garbage collection in NetBeans\n");
        doc.add  ("Each value is the average from a series of runs, and the standard deviation is shown by the bracket\n");
        doc.add  ("bracing the value end of each bar.<P>");
        doc.add  ("The current tests are only of NetBeans start-up, running NetBeans multiple times with the line switch ");
        doc.add  ("to stop it once the main window comes up.  Since there is currently no extensive and functioning test");
        doc.add  ("that will drive the NetBeans UI in memory consuming ways, these charts reflect startup performance only.");
        doc.add  ("<B>Note:</B> The numbers may be wrong on Windows.  There is currently a bug in the window system that ");
        doc.add  ("causes all of the components in the main window to dock themselves into new frames just prior to the ");
        doc.add  ("IDE shutting itself down.  This does not happen with all sets of java lineswitches, but it likely ");
        doc.add  ("impacts the session time, since requesting new frames from the operating system is expensive.<P>");
        doc.add (new HTMLImage ("gcseconds.png"));
        doc.add ("<P>");
        doc.add (new HTMLImage ("gccount.png"));
        doc.add ("<P>");
        doc.add (new HTMLImage ("gcavgtime.png"));
        doc.add ("<P>");
        doc.add (new HTMLImage ("sessionlength.png"));
        doc.add ("<P>");
        return doc;
    }
    
    /**Test execution for debugging  */
    public static void main (String[] args) throws Exception {
        Analyzer al = new GcCharts ();
        al.setOutDir("/home/tb97952/tmp");
        al.setDataFile ("/tmp/SerThing.ser");
        al.execute();
    }

}
