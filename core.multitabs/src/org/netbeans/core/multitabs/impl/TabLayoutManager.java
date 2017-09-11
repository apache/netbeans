/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.multitabs.impl;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        layoutTimer = new Timer( 350, new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                doLayout();
            }
        });
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
            ArrayList<Integer> tabs = new ArrayList<Integer>( tabCount );
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
            ArrayList<Integer>[] rowIndexes = new ArrayList[rowCount];
            for( int i=0; i<rowCount; i++ ) {
                rowIndexes[i] = new ArrayList<Integer>( tabCount );
            }

            for( int i=0; i<tabCount; i++ ) {
                TabData td = tabModel.getTab( i );

                ProjectProxy p = projectSupport.getProjectForTab( td );
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
