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

package org.netbeans.lib.profiler.ui.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Date;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import org.netbeans.lib.profiler.charts.axis.AxisComponent;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.ChartDecorator;
import org.netbeans.lib.profiler.charts.ChartItem;
import org.netbeans.lib.profiler.charts.ChartSelectionModel;
import org.netbeans.lib.profiler.charts.ItemsModel;
import org.netbeans.lib.profiler.charts.swing.LongRect;
import org.netbeans.lib.profiler.charts.swing.CrossBorderLayout;
import org.netbeans.lib.profiler.charts.PaintersModel;
import org.netbeans.lib.profiler.charts.axis.BytesAxisUtils;
import org.netbeans.lib.profiler.charts.axis.BytesMarksPainter;
import org.netbeans.lib.profiler.charts.xy.BytesXYItemMarksComputer;
import org.netbeans.lib.profiler.charts.axis.TimeMarksPainter;
import org.netbeans.lib.profiler.charts.axis.TimelineMarksComputer;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.XYItem;
import org.netbeans.lib.profiler.charts.xy.XYItemPainter;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem;
import org.netbeans.lib.profiler.results.DataManagerListener;
import org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYChart;
import org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYItemPainter;
import org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYSelectionOverlay;
import org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipModel;
import org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipOverlay;
import org.netbeans.lib.profiler.ui.charts.xy.ProfilerXYTooltipPainter;
import org.netbeans.lib.profiler.ui.components.ColorIcon;
import org.netbeans.lib.profiler.ui.monitor.VMTelemetryModels;
import org.netbeans.lib.profiler.ui.swing.InvisibleToolbar;



/**
 *
 * @author Jiri Sedlacek
 */
public final class MemoryGraphPanel extends GraphPanel {
    
    private final Color SIZE_COLOR = ColorFactory.getPredefinedColor(0);
    private final Color[] SIZE_GRADIENT = ColorFactory.getPredefinedGradient(0);
    private final Color USED_COLOR = ColorFactory.getPredefinedColor(1);
    private final Color[] USED_GRADIENT = ColorFactory.getPredefinedGradient(1);

    private ProfilerXYChart chart;
    private Action[] chartActions;

    private final VMTelemetryModels models;
    private final DataManagerListener listener;

    private final boolean smallPanel;


    // --- Constructors --------------------------------------------------------
    
    public static MemoryGraphPanel createBigPanel(VMTelemetryModels models) {
        return new MemoryGraphPanel(models, false, null);
    }
    
    public static MemoryGraphPanel createSmallPanel(VMTelemetryModels models,
                                             Action chartAction) {
        return new MemoryGraphPanel(models, true, chartAction);
    }

    private MemoryGraphPanel(VMTelemetryModels models,
                             boolean smallPanel, Action chartAction) {

        // Save models and panel type
        this.models = models;
        this.smallPanel = smallPanel;

        // Create UI
        initComponents(chartAction);

        // Register listener
        listener = new DataManagerListener() {
            public void dataChanged() { updateData(); }
            public void dataReset() { resetData(); }
        };
        models.getDataManager().addDataListener(listener);

        // Initialize chart & legend
        resetData();
    }


    // --- GraphPanel implementation -------------------------------------------

    public Action[] getActions() {
        return chartActions;
    }
    
    public void cleanup() {
        models.getDataManager().removeDataListener(listener);
    }


    // --- Private implementation ----------------------------------------------

    private void updateData() {
        if (smallPanel) {
            if (chart.fitsWidth()) {
                VMTelemetryDataManager manager = models.getDataManager();
                long[] timestamps = manager.timeStamps;
                if (timestamps[manager.getItemCount() - 1] - timestamps[0] >=
                    SMALL_CHART_FIT_TO_WINDOW_PERIOD)
                        chart.setFitsWidth(false);
            }
        } else {
        }
    }

    private void resetData() {
        if (smallPanel) {
            chart.setScale(INITIAL_CHART_SCALEX, 1);
            chart.setOffset(0, 0);
            chart.setFitsWidth(true);
        } else {
            chart.setScale(INITIAL_CHART_SCALEX, 1);
            chart.setOffset(0, 0);
            chart.setFitsWidth(false);
        }
        chart.setInitialDataBounds(new LongRect(System.currentTimeMillis(), 0,
                                       2500, GraphsUI.HEAP_SIZE_INITIAL_VALUE));
    }

    
    private void initComponents(final Action chartAction) {
        // Painters model
        PaintersModel paintersModel = createMemoryPaintersModel();

        // Chart
        chart = createChart(models.memoryItemsModel(),
                            paintersModel, smallPanel);
        chart.setBackground(GraphsUI.CHART_BACKGROUND_COLOR);
        chart.setViewInsets(new Insets(10, 0, 0, 0));
        
        chart.addPreDecorator(createMaxHeapDecorator());

        // Horizontal axis
        AxisComponent hAxis =
                new AxisComponent(chart, new TimelineMarksComputer(
                         models.memoryItemsModel().getTimeline(),
                         chart.getChartContext(), SwingConstants.HORIZONTAL),
                         new TimeMarksPainter(),
                         SwingConstants.NORTH, AxisComponent.MESH_FOREGROUND);
        hAxis.setForeground(Color.GRAY);

        // Vertical axis
        XYItem memoryItem = models.memoryItemsModel().getItem(0);
        XYItemPainter memoryPainter = (XYItemPainter)paintersModel.getPainter(memoryItem);
        AxisComponent vAxis =
                new AxisComponent(chart, new BytesXYItemMarksComputer(
                         memoryItem, memoryPainter, chart.getChartContext(),
                         SwingConstants.VERTICAL),
                         new BytesMarksPainter(), SwingConstants.WEST,
                         AxisComponent.MESH_FOREGROUND);
        vAxis.setForeground(Color.GRAY);

        // Chart panel (chart & axes)
        JPanel chartPanel = new JPanel(new CrossBorderLayout());
        chartPanel.setBackground(GraphsUI.CHART_BACKGROUND_COLOR);
        chartPanel.setBorder(BorderFactory.createMatteBorder(
                             10, 10, 5, 5, GraphsUI.CHART_BACKGROUND_COLOR));
        chartPanel.add(chart, new Integer[] { SwingConstants.CENTER });
        chartPanel.add(hAxis, new Integer[] { SwingConstants.NORTH,
                                              SwingConstants.NORTH_EAST,
                                              SwingConstants.NORTH_WEST });
        chartPanel.add(vAxis, new Integer[] { SwingConstants.WEST,
                                              SwingConstants.SOUTH_WEST });
        
        JScrollBar scroller = new JScrollBar(JScrollBar.HORIZONTAL);
        chart.attachHorizontalScrollBar(scroller);
        chartPanel.add(scroller, new Integer[] { SwingConstants.SOUTH });

        // Small panel UI
        if (smallPanel) {

        // Big panel UI
        } else {
            
            // Tooltip support
            ProfilerXYTooltipPainter tooltipPainter = new ProfilerXYTooltipPainter(createTooltipModel());
            chart.addOverlayComponent(new ProfilerXYTooltipOverlay(chart, tooltipPainter));
            chart.getSelectionModel().setHoverMode(ChartSelectionModel.HOVER_EACH_NEAREST);

            // Hovering support
            ProfilerXYSelectionOverlay selectionOverlay = new ProfilerXYSelectionOverlay();
            chart.addOverlayComponent(selectionOverlay);
            selectionOverlay.registerChart(chart);
            chart.getSelectionModel().setMoveMode(ChartSelectionModel.SELECTION_LINE_V);

            // Chart container (chart panel & scrollbar)
            JPanel chartContainer = new JPanel(new BorderLayout());
            chartContainer.setBorder(BorderFactory.createEmptyBorder());
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            
            // Side panel
            JPanel sidePanel = new JPanel(new BorderLayout());
            sidePanel.setOpaque(false);
            int h = new JLabel("XXX").getPreferredSize().height; // NOI18N
            sidePanel.setBorder(BorderFactory.createEmptyBorder(h + 17, 0, 0, 10));
            InvisibleToolbar toolbar = new InvisibleToolbar(InvisibleToolbar.VERTICAL);
            toolbar.setOpaque(true);
            toolbar.setBackground(UIUtils.getProfilerResultsBackground());
            toolbar.add(chart.toggleViewAction()).setBackground(UIUtils.getProfilerResultsBackground());
            toolbar.add(chart.zoomInAction()).setBackground(UIUtils.getProfilerResultsBackground());
            toolbar.add(chart.zoomOutAction()).setBackground(UIUtils.getProfilerResultsBackground());
            sidePanel.add(toolbar, BorderLayout.CENTER);      

            // Heap Size
            JLabel heapSizeBig = new JLabel(GraphsUI.HEAP_SIZE_NAME,
                                            new ColorIcon(SIZE_COLOR, Color.
                                            BLACK, 18, 9), SwingConstants.LEADING);
            heapSizeBig.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

            // Used heap
            JLabel usedHeapBig = new JLabel(GraphsUI.USED_HEAP_NAME,
                                            new ColorIcon(USED_COLOR, Color.
                                            BLACK, 18, 9), SwingConstants.LEADING);
            usedHeapBig.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

            // Legend container
            JPanel bigLegendPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 7, 0));
            bigLegendPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 30));
            bigLegendPanel.setOpaque(true);
            bigLegendPanel.setBackground(UIUtils.getProfilerResultsBackground());
            bigLegendPanel.add(heapSizeBig);
            bigLegendPanel.add(usedHeapBig);

            // Master UI
            setLayout(new BorderLayout());
            setBackground(UIUtils.getProfilerResultsBackground());
            JLabel caption = new JLabel(GraphsUI.MEMORY_CAPTION, JLabel.CENTER);
            caption.setFont(caption.getFont().deriveFont(Font.BOLD));
            caption.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
            caption.setOpaque(true);
            caption.setBackground(UIUtils.getProfilerResultsBackground());
            add(caption, BorderLayout.NORTH);
            add(chartContainer, BorderLayout.CENTER);
            add(bigLegendPanel, BorderLayout.SOUTH);
            add(sidePanel, BorderLayout.EAST);

            // Toolbar actions
            chartActions = new Action[] { chart.zoomInAction(),
                                          chart.zoomOutAction(),
                                          chart.toggleViewAction()};

        }

    }
    

    protected ProfilerXYTooltipModel createTooltipModel() {
        return new ProfilerXYTooltipModel() {

            public String getTimeValue(long timestamp) {
                return DATE_FORMATTER.format(new Date(timestamp));
            }

            public int getRowsCount() {
                return 2;
            }

            public String getRowName(int index) {
                switch (index) {
                    case 0:
                        return GraphsUI.HEAP_SIZE_NAME;
                    case 1:
                        return GraphsUI.USED_HEAP_NAME;
                    default:
                        return null;
                }
            }

            public Color getRowColor(int index) {
                switch (index) {
                    case 0:
                        return GraphsUI.HEAP_SIZE_PAINTER_FILL_COLOR;
                    case 1:
                        return GraphsUI.USED_HEAP_PAINTER_FILL_COLOR;
                    default:
                        return null;
                }
            }

            public String getRowValue(int index, long itemValue) {
                return INT_FORMATTER.format(itemValue);
            }

            public String getRowUnits(int index) {
                return BytesAxisUtils.UNITS_B;
            }

            public int getExtraRowsCount() {
                return getRowsCount();
            }

            public String getExtraRowName(int index) {
                return getMaxValueString(getRowName(index));
            }

            public Color getExtraRowColor(int index) {
                return getRowColor(index);
            }

            public String getExtraRowValue(int index) {
                SynchronousXYItem item = models.memoryItemsModel().getItem(index);
                return INT_FORMATTER.format(item.getMaxYValue());
            }

            public String getExtraRowUnits(int index) {
                return getRowUnits(index);
            }

        };
    }

    private PaintersModel createMemoryPaintersModel() {
        // Heap size
        ProfilerXYItemPainter heapSizePainter =
                ProfilerXYItemPainter.absolutePainter(GraphsUI.HEAP_SIZE_PAINTER_LINE_WIDTH,
                                                      SIZE_COLOR,
                                                      SIZE_GRADIENT[0]);
         XYItemPainter hsp = heapSizePainter;

        // Used heap
        ProfilerXYItemPainter usedHeapPainter =
                ProfilerXYItemPainter.absolutePainter(GraphsUI.USED_HEAP_PAINTER_LINE_WIDTH,
                                                      USED_COLOR,
                                                      USED_GRADIENT[0]);
        XYItemPainter uhp = usedHeapPainter;

        // Model
        ItemsModel items = models.memoryItemsModel();
        PaintersModel model = new PaintersModel.Default(
                                            new ChartItem[] { items.getItem(0),
                                                              items.getItem(1) },
                                            new XYItemPainter[] { hsp, uhp });

        return model;
    }

    private ChartDecorator createMaxHeapDecorator() {
        return new ChartDecorator() {
            public void paint(Graphics2D g, Rectangle dirtyArea,
                              ChartContext context) {

                int limitHeight = Utils.checkedInt(
                                  context.getViewY(models.getDataManager().
                                  maxHeapSize));
                if (limitHeight <= context.getViewportHeight()) {
                    g.setColor(GraphsUI.HEAP_LIMIT_FILL_COLOR);
                    if (context.isBottomBased())
                        g.fillRect(0, 0, context.getViewportWidth(), limitHeight);
                    else
                        g.fillRect(0, limitHeight, context.getViewportWidth(),
                                   context.getViewportHeight() - limitHeight);
                }
            }
        };
    }

}
