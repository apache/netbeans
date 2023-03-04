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
 * NbChart.java
 *
 * Created on October 16, 2002, 10:32 PM
 */

package org.netbeans.performance.impl.chart;
import java.util.*;
import com.jrefinery.chart.*;
import com.jrefinery.data.*;
import java.awt.Font;
/** A convenience extension of JFreeChart that hides some of the
 *  complexity of creating charts where we do things in a
 *  fairly standard way.
 *
 * @author  Tim Boudreau
 */
public class NbChart extends JFreeChart {

    /** Creates a new instance of NbChart */
    public NbChart(String title, String xAxisTitle, String yAxisTitle, NbStatisticalDataset data) {
        super (title,  new Font("Helvetica", Font.BOLD, 14),
          new VerticalCategoryPlot (data,
            new HorizontalCategoryAxis(xAxisTitle),
            new VerticalNumberAxis(yAxisTitle),
            new VerticalStatisticalBarRenderer()),
            true);
    }

}
