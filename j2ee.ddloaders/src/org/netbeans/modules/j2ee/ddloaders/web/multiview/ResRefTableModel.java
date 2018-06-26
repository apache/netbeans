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

/** ResRefTableModel - table model for resource references
 *
 * Created on April 11, 2005
 * @author  mkuchtiak
 */
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class ResRefTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(ResRefTableModel.class,"TTL_ResRefName"),
            NbBundle.getMessage(ResRefTableModel.class,"TTL_ResType"),
            NbBundle.getMessage(ResRefTableModel.class,"TTL_ResAuth"),
            NbBundle.getMessage(ResRefTableModel.class,"TTL_ResSharingScope"),
            NbBundle.getMessage(ResRefTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

    @Override
	public void setValueAt(Object value, int row, int column)
	{
		ResourceRef param = getResourceRef(row);

		if (column == 0) param.setResRefName((String)value);
		else if (column == 1) param.setResType((String)value);
		else if (column == 2) param.setResAuth((String)value);
                else if (column == 3) param.setResSharingScope((String)value);
		else param.setDescription((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		ResourceRef param = getResourceRef(row);

		if (column == 0) return param.getResRefName();
		else if (column == 1) return param.getResType();
		else if (column == 2) return param.getResAuth();
                else if (column == 3) {
                    String scope = param.getResSharingScope();
                    return ("Unshareable".equals(scope)?scope:"Shareable"); //NOI18N
                }
		else {
                    String desc = param.getDefaultDescription();
                    return desc==null?null:desc.trim();
                }
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                WebApp webApp = (WebApp)getParent();
                ResourceRef param=(ResourceRef)webApp.createBean("ResourceRef"); //NOI18N
                param.setResRefName((String)values[0]);
                param.setResType((String)values[1]);
                param.setResAuth((String)values[2]);
                param.setResSharingScope((String)values[3]);
                String desc = (String)values[4];
                param.setDescription(desc.length()>0?desc:null);
                webApp.addResourceRef(param);
                getChildren().add(param);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return param;
            } catch (ClassNotFoundException ex) {}
            return null;
	}

	public void editRow(int row, Object[] values)
	{
                ResourceRef param = getResourceRef(row);
                param.setResRefName((String)values[0]);
                param.setResType((String)values[1]);
                param.setResAuth((String)values[2]);
                String scope = (String)values[3];
                String oldScope = param.getResSharingScope();
                if (oldScope == null && "Unshareable".equals(scope)){//NOI18N
                    param.setResSharingScope(scope);
                } else if (!scope.equals(oldScope)){
                    param.setResSharingScope(scope);
                }
                String desc = (String)values[4];
                param.setDescription(desc.length()>0?desc:null);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            WebApp webApp = (WebApp)getParent();
            webApp.removeResourceRef(getResourceRef(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
        
        ResourceRef getResourceRef(int row) {
            return (ResourceRef)getChildren().get(row);
        }
}
