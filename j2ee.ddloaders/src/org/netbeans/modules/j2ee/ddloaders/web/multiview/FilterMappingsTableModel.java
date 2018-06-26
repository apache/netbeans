/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
