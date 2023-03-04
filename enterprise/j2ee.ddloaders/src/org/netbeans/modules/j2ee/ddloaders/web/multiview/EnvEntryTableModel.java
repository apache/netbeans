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

/** EnvEntryTableModel - table model for env-entries
 *
 * Created on April 11, 2005
 * @author  mkuchtiak
 */
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class EnvEntryTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(EnvEntryTableModel.class,"TTL_EnvEntryName"),
            NbBundle.getMessage(EnvEntryTableModel.class,"TTL_EnvEntryType"),
            NbBundle.getMessage(EnvEntryTableModel.class,"TTL_EnvEntryValue"),
            NbBundle.getMessage(EnvEntryTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

    @Override
	public void setValueAt(Object value, int row, int column)
	{
		EnvEntry param = (EnvEntry)getChildren().get(row);

		if (column == 0) param.setEnvEntryName((String)value);
		else if (column == 1) param.setEnvEntryType((String)value);
		else if (column == 2) param.setEnvEntryValue((String)value);
		else param.setDescription((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		EnvEntry param = (EnvEntry)getChildren().get(row);

		if (column == 0) return param.getEnvEntryName();
		else if (column == 1) return param.getEnvEntryType();
		else if (column == 2) return param.getEnvEntryValue();
		else {
                    String desc = param.getDefaultDescription();
                    return desc==null?null:desc.trim();
                }
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                WebApp webApp = (WebApp)getParent();
                EnvEntry param=(EnvEntry)webApp.createBean("EnvEntry"); //NOI18N
                param.setEnvEntryName((String)values[0]);
                param.setEnvEntryType((String)values[1]);
                String value = (String)values[2];
                param.setEnvEntryValue(value.length()>0?value:null);
                String desc = (String)values[3];
                param.setDescription(desc.length()>0?desc:null);
                webApp.addEnvEntry(param);
                getChildren().add(param);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return param;
            } catch (ClassNotFoundException ex) {}
            return null;
	}

	public void editRow(int row, Object[] values)
	{
                EnvEntry param = (EnvEntry)getChildren().get(row);
                param.setEnvEntryName((String)values[0]);
                param.setEnvEntryType((String)values[1]);
                String value = (String)values[2];
                param.setEnvEntryValue(value.length()>0?value:null);
                String desc = (String)values[3];
                param.setDescription(desc.length()>0?desc:null);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            WebApp webApp = (WebApp)getParent();
            webApp.removeEnvEntry((EnvEntry)getChildren().get(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
        
        EnvEntry getEnvEntry(int row) {
            return (EnvEntry)getChildren().get(row);
        }
}
