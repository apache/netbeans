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

/** EjbRefTableModel - table model for EJB references and EJB Local References
 *
 * Created on April 11, 2005
 * @author  mkuchtiak
 */
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class EjbRefTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbRefName"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbRefType"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbInterfaceType"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbHome"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbInterface"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbLink"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

    @Override
	public void setValueAt(Object value, int row, int column)
	{
		CommonDDBean ref = getEjbRef(row);
                if (ref instanceof EjbRef) {
                    EjbRef param = (EjbRef)ref;
                    if (column == 0) param.setEjbRefName((String)value);
                    else if (column == 1) param.setEjbRefType((String)value);
                    else if (column == 3) param.setHome((String)value);
                    else if (column == 4) param.setRemote((String)value);
                    else if (column == 5) param.setEjbLink((String)value);
                    else if (column == 6) param.setDescription((String)value);
                } else {
                    EjbLocalRef param = (EjbLocalRef)ref;
                    if (column == 0) param.setEjbRefName((String)value);
                    else if (column == 1) param.setEjbRefType((String)value);
                    else if (column == 3) param.setLocalHome((String)value);
                    else if (column == 4) param.setLocal((String)value);
                    else if (column == 5) param.setEjbLink((String)value);
                    else if (column == 6) param.setDescription((String)value);
                }
	}


	public Object getValueAt(int row, int column)
	{
                CommonDDBean ref = getEjbRef(row);
                if (ref instanceof EjbRef) {
                    EjbRef param = (EjbRef)ref;
                    if (column == 0) return param.getEjbRefName();
                    else if (column == 1) return param.getEjbRefType();
                    else if (column == 2) return "Remote"; //NOI18N
                    else if (column == 3) return param.getHome();
                    else if (column == 4) return param.getRemote();
                    else if (column == 5) return param.getEjbLink();
                    else {
                        String desc = param.getDefaultDescription();
                        return desc==null?null:desc.trim();
                    }
                } else {
                    EjbLocalRef param = (EjbLocalRef)ref;
                    if (column == 0) return param.getEjbRefName();
                    else if (column == 1) return param.getEjbRefType();
                    else if (column == 2) return "Local"; //NOI18N
                    else if (column == 3) return param.getLocalHome();
                    else if (column == 4) return param.getLocal();
                    else if (column == 5) return param.getEjbLink();
                    else {
                        String desc = param.getDefaultDescription();
                        return desc==null?null:desc.trim();
                    }
                }
	}
        
	public CommonDDBean addRow(Object[] values) {
            try {
                CommonDDBean param=null;
                WebApp webApp = (WebApp)getParent();
                String interfaceType = (String)values[2];
                if ("Remote".equals(interfaceType)) {
                    param=webApp.createBean("EjbRef"); //NOI18N
                    ((EjbRef)param).setEjbRefName((String)values[0]);
                    ((EjbRef)param).setEjbRefType((String)values[1]);
                    ((EjbRef)param).setHome((String)values[3]);
                    ((EjbRef)param).setRemote((String)values[4]);
                    String link = (String)values[5];
                    if (link.length()>0) ((EjbRef)param).setEjbLink(link);
                    String desc = (String)values[6];
                    if (desc.length()>0) ((EjbRef)param).setDescription(desc);
                    int row = webApp.sizeEjbRef();
                    webApp.addEjbRef((EjbRef)param);
                    getChildren().add(row,param);
                    fireTableRowsInserted(row, row);
                } else {
                    param=webApp.createBean("EjbLocalRef"); //NOI18N
                    ((EjbLocalRef)param).setEjbRefName((String)values[0]);
                    ((EjbLocalRef)param).setEjbRefType((String)values[1]);
                    ((EjbLocalRef)param).setLocalHome((String)values[3]);
                    ((EjbLocalRef)param).setLocal((String)values[4]);
                    String link = (String)values[5];
                    if (link.length()>0) ((EjbLocalRef)param).setEjbLink(link);
                    String desc = (String)values[6];
                    if (desc.length()>0) ((EjbLocalRef)param).setDescription(desc);
                    webApp.addEjbLocalRef((EjbLocalRef)param);
                    getChildren().add(param);
                    int row = getRowCount() - 1;
                    fireTableRowsInserted(row, row);
                }
                return param;
            } catch (ClassNotFoundException ex) {}
            return null;
	}

	public void editRow(int row, Object[] values)
	{
            CommonDDBean ref = getEjbRef(row);
            if (ref instanceof EjbRef) {
                EjbRef param = (EjbRef)ref;
                param.setEjbRefName((String)values[0]);
                param.setEjbRefType((String)values[1]);
                param.setHome((String)values[3]);
                param.setRemote((String)values[4]);
                String ejbLink = (String)values[5];
                param.setEjbLink(ejbLink.length()>0?ejbLink:null);
                String desc = (String)values[6];
                param.setDescription(desc.length()>0?desc:null);
                fireTableRowsUpdated(row,row);
            } else {
                EjbLocalRef param = (EjbLocalRef)ref;
                param.setEjbRefName((String)values[0]);
                param.setEjbRefType((String)values[1]);
                param.setLocalHome((String)values[3]);
                param.setLocal((String)values[4]);
                String ejbLink = (String)values[5];
                param.setEjbLink(ejbLink.length()>0?ejbLink:null);
                String desc = (String)values[6];
                param.setDescription(desc.length()>0?desc:null);
                fireTableRowsUpdated(row,row);
            }    
	}
        
	public void removeRow(int row) {
            WebApp webApp = (WebApp)getParent();
            CommonDDBean ref = getEjbRef(row);
            if (ref instanceof EjbRef) {
                webApp.removeEjbRef((EjbRef)ref);
            } else {
                webApp.removeEjbLocalRef((EjbLocalRef)ref);
            }
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
        
        CommonDDBean getEjbRef(int row) {
            return (CommonDDBean)getChildren().get(row);
        }
}
