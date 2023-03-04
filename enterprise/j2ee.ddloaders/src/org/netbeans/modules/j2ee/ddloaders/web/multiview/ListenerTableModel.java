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

// Netbeans
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class ListenerTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(ListenerTableModel.class,"TTL_ListenerClass"),
            NbBundle.getMessage(ListenerTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

    @Override
	public void setValueAt(Object value, int row, int column)
	{
		Listener listener = (Listener)getChildren().get(row);

		if (column == 0) listener.setListenerClass((String)value);
		else listener.setDescription((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		Listener listener = (Listener)getChildren().get(row);

		if (column == 0) return listener.getListenerClass();
		else {
                    String desc = listener.getDefaultDescription();
                    return (desc==null?null:desc.trim());
                }
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                Listener listener = (Listener)((WebApp)getParent()).createBean("Listener"); //NOI18N
                listener.setListenerClass((String)values[0]);
                String desc = (String)values[1];
                if (desc.length()>0) listener.setDescription(desc);
                ((WebApp)getParent()).addListener(listener);
                getChildren().add(listener);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return listener;
            } catch (ClassNotFoundException ex) {}
            return null;
	}


	public void editRow(int row, Object[] values)
	{
                Listener listener = (Listener)getChildren().get(row);
		listener.setListenerClass((String)values[0]);
                String desc=(String)values[1];
                if (desc.length()>0) listener.setDescription(desc);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            ((WebApp)getParent()).removeListener((Listener)getChildren().get(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
}
