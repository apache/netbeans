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
