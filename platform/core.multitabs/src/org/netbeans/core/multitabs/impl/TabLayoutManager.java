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
package org.netbeans.core.multitabs.impl;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.core.multitabs.Settings;
import org.netbeans.core.multitabs.impl.ProjectSupport.ProjectProxy;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;

/**
 *
 * @author S. Aubrecht
 */
abstract class TabLayoutManager {

    protected final List<SingleRowTabTable> rows;
    protected final Container container;
    protected final TabDataModel tabModel;
    private final Timer layoutTimer;

    public static TabLayoutManager create( List<SingleRowTabTable> rows, Container container, TabDataModel tabModel ) {
        if( Settings.getDefault().isTabRowPerProject() )
            return new RowPerProjectTabLayoutManager( rows, container, tabModel );
        return new FlowTabLayoutManager( rows, container, tabModel );
    }

    protected TabLayoutManager( List<SingleRowTabTable> rows, Container container, TabDataModel tabModel ) {
        this.rows = rows;
        this.container = container;
        this.tabModel = tabModel;
        layoutTimer = new Timer(350, (ActionEvent e) -> doLayout());
        layoutTimer.setRepeats( false );
    }

    Rectangle getTabBounds( int tabIndex ) {
        TabData tab = tabModel.getTab( tabIndex );
        if( null == tab )
            return null;
        for( SingleRowTabTable table : rows ) {
            if( table.hasTabIndex( tabIndex ) ) {
                Rectangle rect = table.getTabBounds( tabIndex );
                if( null != rect ) {
                    rect = SwingUtilities.convertRectangle( table, rect, container );
                }
                return rect;
            }
        }
        return null;
    }

    TabData getTabAt( Point p ) {
        for( TabTable table : rows ) {
            Point location = SwingUtilities.convertPoint( container, p, table );
            if( table.contains( location ) ) {
                return table.getTabAt( location );
            }
        }
        return null;
    }

    final void resizeContainer() {
        JComponent c = ( JComponent ) container.getParent();
        c.invalidate();
        c.revalidate();
        c.doLayout();
    }

    protected final void invalidate() {
        synchronized( layoutTimer ) {
            if( layoutTimer.isRunning() ) {
                layoutTimer.restart();
                return;
            }
            layoutTimer.start();
        }
    }

    protected abstract void doLayout();

    private static class FlowTabLayoutManager extends TabLayoutManager {

        private final TabDataRenderer renderer = new TabDataRenderer();
        private final int columnPadding;

        FlowTabLayoutManager( List<SingleRowTabTable> rows, Container container, TabDataModel tabModel ) {
            super( rows, container, tabModel );
            columnPadding = new JTable().getIntercellSpacing().width;

            container.addComponentListener( new ComponentListener() {

                @Override
                public void componentResized( ComponentEvent e ) {
                    invalidate();
                }

                @Override
                public void componentMoved( ComponentEvent e ) {
                }

                @Override
                public void componentShown( ComponentEvent e ) {
                }

                @Override
                public void componentHidden( ComponentEvent e ) {
                }
            });
        }

        @Override
        protected void doLayout() {
            final int rowCount = rows.size();
            int availableWidth = container.getWidth();
            int currentRow = 0;
            int[] rowWidth = new int[rowCount];
            int[] lastIndexInRow = new int[rowCount];
            Arrays.fill( lastIndexInRow, -1 );
            int tabCount = tabModel.size();
            int[] tabWidth = new int[tabCount];
            for( int i=0; i<tabCount; i++ ) {
                TabData td = tabModel.getTab( i );

                int colWidth = renderer.getPreferredWidth( td ) + columnPadding;
                tabWidth[i] = colWidth;
            }
            int tabIndex = 0;
            while( tabIndex < tabCount ) {
                int width = tabWidth[tabIndex];
                if( width + rowWidth[currentRow] <= availableWidth || width > availableWidth ) {
                    lastIndexInRow[currentRow] = tabIndex;
                    rowWidth[currentRow] += width;
                    tabIndex++;
                } else {
                    currentRow++;
                    if( currentRow >= rowCount )
                        break;
                }
            }

            if( tabIndex < tabCount ) {
                //there are too many tabs to fit available width
                currentRow = 0;
                while( true ) {

                    //shift tabs
                    lastIndexInRow[currentRow]++;
                    rowWidth[currentRow] += tabWidth[lastIndexInRow[currentRow]];
                    availableWidth = Math.max( availableWidth, rowWidth[currentRow] );

                    for( int i=currentRow+1; i<rowCount; i++ ) {
                        lastIndexInRow[i] = -1;
                        rowWidth[i] = 0;
                    }

                    tabIndex = lastIndexInRow[currentRow]+1;
                    int nextRow = currentRow+1;
                    while( tabIndex < tabCount ) {
                        int width = tabWidth[tabIndex];
                        if( width + rowWidth[nextRow] <= availableWidth || width > availableWidth ) {
                            lastIndexInRow[nextRow] = tabIndex;
                            rowWidth[nextRow] += width;
                            tabIndex++;
                        } else {
                            nextRow++;
                            if( nextRow >= rowCount )
                                break;
                        }
                    }
                    if( tabIndex >= tabCount )
                        break;

                    currentRow++;
                    if( currentRow >= rowCount-1 ) {
                        currentRow = 0;
                    }
                }
            }
            ArrayList<Integer> tabs = new ArrayList<>( tabCount );
            int prevTab = -1;
            for( int i=0; i<lastIndexInRow.length; i++ ) {
                tabs.clear();
                for( int j=prevTab+1; j<=lastIndexInRow[i]; j++ ) {
                    tabs.add( j );
                }
                rows.get( i ).setTabs( tabs );
                prevTab = lastIndexInRow[i];
            }
            resizeContainer();
        }

    }

    private static class RowPerProjectTabLayoutManager extends TabLayoutManager {

        public RowPerProjectTabLayoutManager( List<SingleRowTabTable> rows, Container container, TabDataModel tabModel ) {
            super( rows, container, tabModel );
        }

        @Override
        protected void doLayout() {
            if( rows.isEmpty() )
                return;
            
            ProjectSupport projectSupport = ProjectSupport.getDefault();
            List<ProjectProxy> projects = Arrays.asList( projectSupport.getOpenProjects() );

            final int tabCount = tabModel.size();
            final int rowCount = rows.size();
            List<Integer>[] rowIndexes = new ArrayList[rowCount];
            for( int i=0; i<rowCount; i++ ) {
                rowIndexes[i] = new ArrayList<>( tabCount );
            }

            Map<TabData, ProjectProxy> tab2Project = projectSupport.tryGetProjectsForTabs(tabModel.getTabs());

            for( int i=0; i<tabCount; i++ ) {
                TabData td = tabModel.getTab( i );

                ProjectProxy p = tab2Project.get(td);
                int index = projects.indexOf( p );
                if( index < 0 || index >= rowIndexes.length-1 || rowCount == 1 )
                    index = 0;
                else
                    index++;
                rowIndexes[index].add( i );
            }

            for( int i=0; i<rowCount; i++ ) {
                rows.get( i ).setTabs( rowIndexes[i] );
            }

            resizeContainer();
        }
    }
}
