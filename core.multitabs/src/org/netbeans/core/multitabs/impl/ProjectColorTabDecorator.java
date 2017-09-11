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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
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

    private static final Map<Object, Color> project2color = new WeakHashMap<Object, Color>(10);
    private static final Map<TabData, Color> tab2color = new WeakHashMap<TabData, Color>(10);
    private static final List<Color> backGroundColors;
    private final static ChangeListener projectsListener = new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
            updateColorMapping();
        }
    };

    static {
        backGroundColors = new ArrayList<Color>( 10 );
        backGroundColors.add( new Color( 216, 255, 237 ) );
        backGroundColors.add( new Color( 255, 221, 221 ) );
        backGroundColors.add( new Color( 255, 247, 214 ) );
        backGroundColors.add( new Color( 216, 239, 255 ) );
        backGroundColors.add( new Color( 241, 255, 209 ) );
        backGroundColors.add( new Color( 255, 225, 209 ) );
        backGroundColors.add( new Color( 228, 255, 216 ) );
        backGroundColors.add( new Color( 227, 255, 158 ) );
        backGroundColors.add( new Color( 238, 209, 255 ) );

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
        Color res = null;
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
        return null == getBackground( tab, selected ) ? null : Color.black;
    }

    @Override
    public void paintAfter( TabData tab, Graphics g, Rectangle tabRect, boolean isSelected ) {
        if( !isSelected || !Settings.getDefault().isSameProjectSameColor() )
            return;
        Color c = null;
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
        rect.y += rect.height - 3;
        rect.grow( -1, -1 );
        g.fillRect( rect.x, rect.y, rect.width, rect.height );
    }

    private static void updateColorMapping() {
        ProjectProxy[] projects = ProjectSupport.getDefault().getOpenProjects();
        synchronized( project2color ) {
            Map<Object, Color> oldColors = new HashMap<Object, Color>( project2color );
            project2color.clear();
            List<Color> availableColors = new ArrayList<Color>( backGroundColors );

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
        ProjectProxy p = ProjectSupport.getDefault().getProjectForTab( tab );
        if( null != p ) {
            synchronized( project2color ) {
                return project2color.get( p.getToken() );
            }
        }
        return null;
    }
}
