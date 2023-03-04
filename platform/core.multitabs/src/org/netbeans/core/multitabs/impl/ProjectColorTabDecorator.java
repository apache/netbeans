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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.multitabs.Settings;
import org.netbeans.core.multitabs.TabDecorator;
import org.netbeans.core.multitabs.impl.ProjectSupport.ProjectProxy;
import org.netbeans.swing.tabcontrol.TabData;
import org.openide.util.lookup.ServiceProvider;

/**
 * Files from the same project have the same background in their tabs. There are
 * several hard-coded background colors. Each project is assigned a background color
 * for its files. If there are more projects than available background colors the extra
 * projects have the default background color.
 *
 * http://netbeans.org/bugzilla/show_bug.cgi?id=153625
 * 
 * @author S. Aubrecht
 */
@ServiceProvider(service=TabDecorator.class)
public class ProjectColorTabDecorator extends TabDecorator {

    private static final Map<Object, Color> project2color = new WeakHashMap<>(10);
    private static final Map<TabData, Color> tab2color = new WeakHashMap<>(10);
    private static final List<Color> backGroundColors;
    private static Color foregroundColor;
    private static final ChangeListener projectsListener = (ChangeEvent e) -> updateColorMapping();

    static {
        backGroundColors = new ArrayList<>( 10 );

        // load background colors from UI defaults if available
        if (UIManager.getColor("nb.multitabs.project.1.background") != null) {
            for (int i = 1; i <= 100; i++) {
                Color color = UIManager.getColor("nb.multitabs.project." + i + ".background");
                if (color == null) {
                    break;
                }
                backGroundColors.add(color);
            }
        } else {
            backGroundColors.add( new Color( 216, 255, 237 ) );
            backGroundColors.add( new Color( 255, 221, 221 ) );
            backGroundColors.add( new Color( 255, 247, 214 ) );
            backGroundColors.add( new Color( 216, 239, 255 ) );
            backGroundColors.add( new Color( 241, 255, 209 ) );
            backGroundColors.add( new Color( 255, 225, 209 ) );
            backGroundColors.add( new Color( 228, 255, 216 ) );
            backGroundColors.add( new Color( 227, 255, 158 ) );
            backGroundColors.add( new Color( 238, 209, 255 ) );
        }

        foregroundColor = UIManager.getColor("nb.multitabs.project.foreground");
        if (foregroundColor == null) {
            foregroundColor = Color.BLACK;
        }

        ProjectSupport projects = ProjectSupport.getDefault();
        if( projects.isEnabled() && Settings.getDefault().isSameProjectSameColor() ) {
            projects.addChangeListener(projectsListener);
        }

        updateColorMapping();
    }

    public static void setActive(boolean active) {
        if( active ) {
            ProjectSupport.getDefault().addChangeListener(projectsListener);
            updateColorMapping();
        } else {
            ProjectSupport.getDefault().removeChangeListener(projectsListener);
        }
    }

    public ProjectColorTabDecorator() {
        updateColorMapping();
    }

    @Override
    public String getText( TabData tab ) {
        return null;
    }

    @Override
    public Icon getIcon( TabData tab ) {
        return null;
    }

    @Override
    public Color getBackground( TabData tab, boolean selected ) {
        if( selected || !Settings.getDefault().isSameProjectSameColor() )
            return null;
        Color res;
        synchronized( tab2color ) {
            res = tab2color.get( tab );
            if( null == res ) {
                res = getColorForTab( tab );
                if( null != res ) {
                    tab2color.put( tab, res );
                }
            }
        }
        return res;
    }

    @Override
    public Color getForeground( TabData tab, boolean selected ) {
        if( selected || !Settings.getDefault().isSameProjectSameColor() )
            return null;
        return null == getBackground( tab, selected ) ? null : foregroundColor;
    }

    @Override
    public void paintAfter( TabData tab, Graphics g, Rectangle tabRect, boolean isSelected ) {
        if( !isSelected || !Settings.getDefault().isSameProjectSameColor() )
            return;
        Color c;
        synchronized( tab2color ) {
            c = tab2color.get( tab );
            if( null == c ) {
                c = getColorForTab( tab );
                if( null == c )
                    return;
                tab2color.put( tab, c );
            }
        }
        g.setColor( c );
        Rectangle rect = new Rectangle( tabRect );
        int underlineHeight = UIManager.getInt("nb.multitabs.underlineHeight"); // NOI18N
        if( underlineHeight > 0 ) {
            // if the selected tab is highlighted with an "underline" (e.g. in FlatLaf)
            // then paint the project color bar at the top of the tab
            rect.height = underlineHeight;
        } else {
            // bottom project color bar
            rect.y += rect.height - 3;
            rect.grow( -1, -1 );
        }
        g.fillRect( rect.x, rect.y, rect.width, rect.height );
    }

    private static void updateColorMapping() {
        ProjectProxy[] projects = ProjectSupport.getDefault().getOpenProjects();
        synchronized( project2color ) {
            Map<Object, Color> oldColors = new HashMap<>( project2color );
            project2color.clear();
            List<Color> availableColors = new ArrayList<>( backGroundColors );

            for( ProjectProxy p : projects ) {
                Color c = oldColors.get( p.getToken() );
                if( null != c ) {
                    availableColors.remove( c );
                    project2color.put( p.getToken(), c );
                }
            }
            if( availableColors.isEmpty() )
                return;
            for( ProjectProxy p : projects ) {
                Color c = project2color.get( p.getToken() );
                if( null == c ) {
                    c = availableColors.get( 0 );
                    project2color.put( p.getToken(), c );
                    availableColors.remove( c );
                    if( availableColors.isEmpty() )
                        break;
                }
            }

        }
    }

    private static Color getColorForTab( TabData tab ) {
        ProjectProxy p = ProjectSupport.getDefault().tryGetProjectForTab(tab);
        if( null != p ) {
            synchronized( project2color ) {
                return project2color.get( p.getToken() );
            }
        }
        return null;
    }
}
