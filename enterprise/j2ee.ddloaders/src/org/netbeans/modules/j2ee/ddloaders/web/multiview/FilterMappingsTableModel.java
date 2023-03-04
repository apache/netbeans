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
package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class FilterMappingsTableModel extends DDBeanTableModel {

    private static final String[] columnNames = {
        NbBundle.getMessage(FilterMappingsTableModel.class, "TTL_FilterName"),
        NbBundle.getMessage(FilterMappingsTableModel.class, "TTL_AppliesTo"),
        NbBundle.getMessage(FilterMappingsTableModel.class, "TTL_DispatcherTypes")
    };

    protected String[] getColumnNames() {
        return columnNames;
    }

    public Object getValueAt(int row, int column) {
        FilterMapping map = (FilterMapping) getChildren().get(row);

        if (column == 0) {
            return map.getFilterName();
        } else if (column == 1) {
            String[] urlPatterns = null;
            try {
                urlPatterns = map.getUrlPatterns();    
            } catch (VersionNotSupportedException ex) {
                if ( map.getUrlPattern() != null ){
                    urlPatterns = new String[]{ map.getUrlPattern()};
                }
            }
            String[] servletNames = null;
            if ( urlPatterns == null ||urlPatterns.length == 0){
                try {
                    servletNames = map.getServletNames();
                } catch (VersionNotSupportedException ex) {
                    if ( map.getServletName()!= null){
                        servletNames = new String[]{ map.getServletName()};
                    }
                }
            }

            String urlPattern = null;
            if ( urlPatterns!= null && urlPatterns.length > 0 ){
                urlPattern = DDUtils.urlPatternList( urlPatterns );
            }
            return urlPattern == null ?
                NbBundle.getMessage(FilterMappingsTableModel.class,
                    "TXT_appliesToServlet", DDUtils.urlPatternList( servletNames)) :
                NbBundle.getMessage(FilterMappingsTableModel.class,
                        "TXT_appliesToUrl", urlPattern);
        } else {
            try {
                return DDUtils.urlPatternList(map.getDispatcher());
            } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
                return null;
            }
        }
    }

    public CommonDDBean addRow(Object[] values) {
        try {
            FilterMapping map = (FilterMapping) ((WebApp) getParent()).createBean("FilterMapping"); //NOI18N
            map.setFilterName((String) values[0]);
            if (values[1] != null) {
                String[] urls = ((String) values[1]).split(",");      // NOI18N
                for (int i=0; i<urls.length ; i++) {
                    urls[i] = urls[i].trim();
                }
                try {
                    map.setUrlPatterns(urls);
                } catch (VersionNotSupportedException ex) {
                    map.setUrlPattern( (String)values[1]);
                }
            }
            if (values[2] != null) {
                try {
                    map.setServletNames((String[]) values[2]);
                } catch (VersionNotSupportedException ex) {
                    String[] value = (String[])values[2];
                    if ( value.length > 0 ){
                       map.setServletName( value[0]);
                    }
                }
            }
            try {
                if (values[3] != null) {
                    map.setDispatcher((String[]) values[3]);
                }
            } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
            }
            ((WebApp) getParent()).addFilterMapping(map);
            getChildren().add(map);
            fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
            return map;
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }

    public void editRow(int row, Object[] values) {
        FilterMapping map = (FilterMapping) getChildren().get(row);
        map.setFilterName((String) values[0]);
        if (values[1] != null) {
            String[] urls = ((String) values[1]).split(",");      // NOI18N
            for (int i = 0; i < urls.length; i++) {
                urls[i] = urls[i].trim();
            }
            try {
                map.setUrlPatterns(urls);
            } catch (VersionNotSupportedException ex) {
                map.setUrlPattern((String) values[1]);
            }
        }
        else {
            map.setUrlPattern( null );
        }
        if (values[2] != null) {
            try {
                map.setServletNames((String[]) values[2]);
            } catch (VersionNotSupportedException ex) {
                String[] value = (String[]) values[2];
                if (value.length > 0) {
                    map.setServletName(value[0]);
                }
            }
        }
        else {
            map.setServletName( null );
        }
        try {
            map.setDispatcher((String[]) values[3]);
        } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
        }
        fireTableRowsUpdated(row, row);
    }

    public void removeRow(int row) {
        ((WebApp) getParent()).removeFilterMapping((FilterMapping) getChildren().get(row));
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);

    }
}
