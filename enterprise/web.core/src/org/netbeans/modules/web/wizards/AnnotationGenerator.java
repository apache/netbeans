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

package org.netbeans.modules.web.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.web.wizards.FilterMappingData.Dispatcher;

/**
 * Helper class for generation of annotations (WebServlet, WebFilter, WebListener, ...)
 * @author Petr Slechta
 */
class AnnotationGenerator {

    private static final String TRUE = "true";
    private static final String CLASS_ANNOTATION = "classAnnotation";
    private static final String INCL_INIT_PARAMS = "includeInitParams";
    private static final String INCL_DISPATCHER = "includeDispatcher";

    private AnnotationGenerator() {
    }

    /**
     * Generate WebServlet annotation in form
     * "@WebServlet(name="xxx", urlPatterns={"/aaa","/ccc"}, initParams={@WebInitParam(name="a", value="1")})"
     * @param data Servlet data
     * @param parameters Map where additional parameters are inserted
     * @return annotation as string
     */
    static String webServlet(ServletData data, Map<String, Object> parameters) {
        String initParams = generInitParams(data.getInitParams());
        if (initParams != null) {
            parameters.put(INCL_INIT_PARAMS, TRUE);
        }
        String res = "@WebServlet("+join(generServletName(data.getClassName(), data.getName()),
                generUrlPatterns(data.getUrlMappings()),
                initParams)+")";
        parameters.put(CLASS_ANNOTATION, res);
        return res;
    }

    // -------------------------------------------------------------------------
    private static String generServletName(String className, String servletName) {
        if (servletName == null || servletName.length() < 1 || servletName.equals(className)) {
            return null;
        } else {
            return "name=\""+servletName+"\"";
        }
    }

    // -------------------------------------------------------------------------
    private static String generUrlPatterns(String[] mappings) {
        if (mappings == null || mappings.length <= 0)
            return null;

        List<String> patterns = new ArrayList<String>();
        for (String s : mappings) {
            patterns.add(s);
        }
        return list("urlPatterns", patterns);
    }

    // -------------------------------------------------------------------------
    private static String generInitParams(String[][] params) {
        if (params == null || params.length <= 0)
            return null;

        List<String> initParams = new ArrayList<String>();
        for (String[] param : params) {
            initParams.add("@WebInitParam(name=\""+param[0]+"\", value=\""+param[1]+"\")");
        }
        return list2("initParams", initParams);
    }

    /**
     * Generate WebFilter annotation in form
     * "@WebFilter(filterName="abc", urlPatterns={"/aaa"}, dispatcherTypes={DispatcherType.ERROR}, initParams={@WebInitParam(name="a", value="1")})"
     * or
     * "@WebFilter(filterName="abc", servletNames={"xyz"}, dispatcherTypes={DispatcherType.ERROR}, initParams={@WebInitParam(name="a", value="1")})"
     * @param data Filter data
     * @param parameters Map where additional parameters are inserted
     * @return annotation as string
     */
    static String webFilter(ServletData data, Map<String, Object> parameters) {
        String initParams = generInitParams(data.getInitParams());
        if (initParams != null) {
            parameters.put(INCL_INIT_PARAMS, TRUE);
        }
        String res = "@WebFilter("+join(generFilterName(data.getName()),
                generMappings(data.getName(), data.getFilterMappings(), parameters),
                initParams)+")";
        parameters.put(CLASS_ANNOTATION, res);
        return res;
    }

    // -------------------------------------------------------------------------
    private static String generFilterName(String name) {
        return (name == null || name.length() < 1) ? null : "filterName=\""+name+"\"";
    }

    // -------------------------------------------------------------------------
    private static String generMappings(String filterName, List<FilterMappingData> mappings, Map<String, Object> parameters) {
        // Let's compute union of all specified dispatchers -- annotation is more
        // restrictive then DD and only one set of disoatchers may be specified.
        Set<String> dispatchers = new HashSet<String>();
        List<String> urlPatterns = new ArrayList<String>();
        List<String> servletNames = new ArrayList<String>();
        for (FilterMappingData item : mappings) {
            if (item.getName().equals(filterName)) {
                if (item.getType() == FilterMappingData.Type.URL) {
                    urlPatterns.add(item.getPattern());
                }
                else if (item.getType() == FilterMappingData.Type.SERVLET) {
                    servletNames.add(item.getPattern());
                }
                for (Dispatcher d : item.getDispatcher()) {
                    if (d != Dispatcher.BLANK)
                        dispatchers.add(d.toString());
                }
            }
        }
        String resDispatchers = dispatchers.isEmpty() ? null :
            "dispatcherTypes={"+join(dispatchers, "DispatcherType.", "")+"}";
        if (resDispatchers != null) {
            parameters.put(INCL_DISPATCHER, TRUE);
        }
        String resUrlPatterns = urlPatterns.isEmpty() ? null : list("urlPatterns", urlPatterns);
        String resServltets = servletNames.isEmpty() ? null : list("servletNames", servletNames);
        return join(resUrlPatterns, resServltets, resDispatchers);
    }

    /**
     * Generate WebListener annotation in form
     * "@WebListener()"
     * @return annotation as string
     */
    static String webListener() {
        return "@WebListener()";
    }

    // -------------------------------------------------------------------------
    // Helper functions
    // -------------------------------------------------------------------------
    private static String join(String... params) {
        boolean first = true;
        StringBuilder res = new StringBuilder();
        for (String s : params) {
            if (s == null || s.length() < 1)
                continue;
            if (!first)
                res.append(", ");
            res.append(s);
            first = false;
        }
        return res.toString();
    }

    // -------------------------------------------------------------------------
    private static String join(Collection<String> params, String left, String right) {
        boolean first = true;
        StringBuilder res = new StringBuilder();
        for (String s : params) {
            if (s == null || s.length() < 1)
                continue;
            if (!first)
                res.append(", ");
            res.append(left);
            res.append(s);
            res.append(right);
            first = false;
        }
        return res.toString();
    }

    // -------------------------------------------------------------------------
    private static String list(String name, List<String> items) {
        StringBuilder res = new StringBuilder();
        res.append(name);
        res.append("={");
        res.append(join(items, "\"", "\""));
        res.append("}");
        return res.toString();
    }

    // -------------------------------------------------------------------------
    private static String list2(String name, List<String> items) {
        StringBuilder res = new StringBuilder();
        res.append(name);
        res.append("={");
        res.append(join(items, "", ""));
        res.append("}");
        return res.toString();
    }

}
