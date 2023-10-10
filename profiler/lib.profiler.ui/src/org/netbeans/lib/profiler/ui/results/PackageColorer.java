/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.lib.profiler.ui.results;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.netbeans.lib.profiler.filters.GenericFilter;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.api.ProfilerStorage;

/**
 *
 * @author Jiri Sedlacek
 */
public final class PackageColorer {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.results.Bundle"); // NOI18N
    private static final String FILTERS_REFLECTION = messages.getString("PackageColorer_FiltersReflection"); // NOI18N
    private static final String FILTERS_JPA = messages.getString("PackageColorer_FiltersJpa"); // NOI18N
    private static final String FILTERS_SERVERS = messages.getString("PackageColorer_FiltersServers"); // NOI18N
    private static final String FILTERS_JAVASE = messages.getString("PackageColorer_FiltersJavaSe"); // NOI18N
    private static final String FILTERS_JAVAEE = messages.getString("PackageColorer_FiltersJavaEe"); // NOI18N
//    private static final String FILTERS_LIQUIBASE = messages.getString("PackageColorer_FiltersLiquibase"); // NOI18N
    // -----
    
    
    private static final String FILTERS_FILE = "filters"; // NOI18N
    
    private static final List<ColoredFilter> COLORS = loadColors();
    
    
    public static boolean registerColor(ColoredFilter color) {
        if (COLORS.contains(color)) return false;
        else return COLORS.add(color);
    }
    
    public static boolean unregisterColor(ColoredFilter color) {
        return COLORS.remove(color);
    }
    
    public static boolean hasRegisteredColors() {
        return !COLORS.isEmpty();
    }
    
    public static List<ColoredFilter> getRegisteredColors() {
        List<ColoredFilter> colors = new ArrayList<>();
        for (ColoredFilter color : COLORS) colors.add(new ColoredFilter(color));
        return colors;
    }
    
    public static void setRegisteredColors(List<ColoredFilter> colors) {
        if (!COLORS.equals(colors)) {
            COLORS.clear();
            COLORS.addAll(colors);
        }
    }
    
    
    public static Color getForeground(String pkg) {
        if (!ProfilerIDESettings.getInstance().isSourcesColoringEnabled()) return null;
        
        for (ColoredFilter color : COLORS)
            if (color.passes(pkg))
                return color.getColor();
        
        return null;
    }
    
    
    private static List<ColoredFilter> loadColors() {
        List<ColoredFilter> colors = new ArrayList<ColoredFilter>() {
            public boolean add(ColoredFilter e) {
                boolean ret = super.add(e);
                if (ret && COLORS != null) storeColors();
                return ret;
            }
            public boolean addAll(Collection<? extends ColoredFilter> c) {
                boolean ret = super.addAll(c);
                if (ret && COLORS != null) storeColors();
                return ret;
            }
            public boolean remove(Object o) {
                boolean ret = super.remove(o);
                if (ret && COLORS != null) storeColors();
                return ret;
            }
        };
        
        // Load persisted filters to Properties
        Properties properties = new Properties();
        try {
            ProfilerStorage.loadGlobalProperties(properties, FILTERS_FILE);
        } catch (IOException e) {
            Logger.getLogger(PackageColorer.class.getName()).log(Level.INFO, null, e);
        }
        
        // Create filter instances from Properties
        if (!properties.isEmpty()) {
            int i = 0;
            while (true) {
                try { colors.add(new ColoredFilter(properties, Integer.toString(i++) + "_")); } // NOI18N
                catch (GenericFilter.InvalidFilterIdException e) { break; }
            }
        }
        
        // Fallback to default filters if no persisted filters
        if (colors.isEmpty()) createDefaultFilters(colors);
        
        return colors;
    }
    
    private static void storeColors() {
        final Properties properties = new Properties();
        final List<ColoredFilter> colors = getRegisteredColors();
        
        new SwingWorker() {
            protected Object doInBackground() throws Exception {
                for (int i = 0; i < colors.size(); i++) try {
                    colors.get(i).store(properties, Integer.toString(i) + "_"); // NOI18N
                } catch (Throwable t) {
                    Logger.getLogger(PackageColorer.class.getName()).log(Level.INFO, null, t);
                }
                
                try {
                    ProfilerStorage.saveGlobalProperties(properties, FILTERS_FILE);
                } catch (IOException e) {
                    Logger.getLogger(PackageColorer.class.getName()).log(Level.INFO, null, e);
                }
                
                return null;
            }
        }.execute();
    }
    
    private static void createDefaultFilters(List<ColoredFilter> colors) {
//        String liquibase = new String("liquibase."); // NOI18N
//        colors.add(new ColoredFilter(FILTERS_LIQUIBASE, liquibase, new Color(135, 135, 135)));
        
        String jpa = new String("org.eclipse.persistence., org.hibernate., org.apache.openjpa."); // NOI18N
        colors.add(new ColoredFilter(FILTERS_JPA, jpa, new Color(135, 135, 135)));
        
        String javaee = new String("javax.servlet., com.sun.enterprise., com.sun.ejb., org.jboss.weld., org.jboss.logging., org.springframework."); // NOI18N
        colors.add(new ColoredFilter(FILTERS_JAVAEE, javaee, new Color(135, 135, 135)));
        
        String servers = new String("org.glassfish., com.sun.appserv., com.sun.gjc., weblogic., com.oracle.weblogic., com.bea., org.apache.tomcat., org.apache.catalina., org.jboss.as., org.eclipse.jetty."); // NOI18N
        colors.add(new ColoredFilter(FILTERS_SERVERS, servers, new Color(135, 135, 135)));
        
        String reflection = new String("java.lang.reflect., sun.reflect., com.sun.proxy."); // NOI18N
        colors.add(new ColoredFilter(FILTERS_REFLECTION, reflection, new Color(180, 180, 180)));
        
        String javase = new String("apple.laf., apple.awt., com.apple., com.sun., java., javax., sun., sunw., org.omg."); // NOI18N
        colors.add(new ColoredFilter(FILTERS_JAVASE, javase, new Color(135, 135, 135)));
    }
    
}
