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
/*
 * JdbcConnectionPool.java
 *
 * Created on November 21, 2004, 4:47 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface JdbcConnectionPool {

        public static final String NAME = "Name";	// NOI18N
	public static final String DATASOURCECLASSNAME = "DatasourceClassname";	// NOI18N
	public static final String RESTYPE = "ResType";	// NOI18N
	public static final String STEADYPOOLSIZE = "SteadyPoolSize";	// NOI18N
	public static final String MAXPOOLSIZE = "MaxPoolSize";	// NOI18N
	public static final String MAXWAITTIMEINMILLIS = "MaxWaitTimeInMillis";	// NOI18N
	public static final String POOLRESIZEQUANTITY = "PoolResizeQuantity";	// NOI18N
	public static final String IDLETIMEOUTINSECONDS = "IdleTimeoutInSeconds";	// NOI18N
	public static final String TRANSACTIONISOLATIONLEVEL = "TransactionIsolationLevel";	// NOI18N
	public static final String ISISOLATIONLEVELGUARANTEED = "IsIsolationLevelGuaranteed";	// NOI18N
	public static final String ISCONNECTIONVALIDATIONREQUIRED = "IsConnectionValidationRequired";	// NOI18N
	public static final String CONNECTIONVALIDATIONMETHOD = "ConnectionValidationMethod";	// NOI18N
	public static final String VALIDATIONTABLENAME = "ValidationTableName";	// NOI18N
	public static final String FAILALLCONNECTIONS = "FailAllConnections";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        public static final String NONTRANSACTIONALCONNECTIONS = "NonTransactionalConnections";	// NOI18N
	public static final String ALLOWNONCOMPONENTCALLERS = "AllowNonComponentCallers";	// NOI18N
        
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setDatasourceClassname(java.lang.String value);

	public java.lang.String getDatasourceClassname();

	public void setResType(java.lang.String value);

	public java.lang.String getResType();

	public void setSteadyPoolSize(java.lang.String value);

	public java.lang.String getSteadyPoolSize();

	public void setMaxPoolSize(java.lang.String value);

	public java.lang.String getMaxPoolSize();

	public void setMaxWaitTimeInMillis(java.lang.String value);

	public java.lang.String getMaxWaitTimeInMillis();

	public void setPoolResizeQuantity(java.lang.String value);

	public java.lang.String getPoolResizeQuantity();

	public void setIdleTimeoutInSeconds(java.lang.String value);

	public java.lang.String getIdleTimeoutInSeconds();

	public void setTransactionIsolationLevel(java.lang.String value);

	public java.lang.String getTransactionIsolationLevel();

	public void setIsIsolationLevelGuaranteed(java.lang.String value);

	public java.lang.String getIsIsolationLevelGuaranteed();

	public void setIsConnectionValidationRequired(java.lang.String value);

	public java.lang.String getIsConnectionValidationRequired();

	public void setConnectionValidationMethod(java.lang.String value);

	public java.lang.String getConnectionValidationMethod();

	public void setValidationTableName(java.lang.String value);

	public java.lang.String getValidationTableName();

	public void setFailAllConnections(java.lang.String value);

	public java.lang.String getFailAllConnections();

	public void setDescription(String value);

	public String getDescription();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();
        
        //Resource 1.2
        public void setNonTransactionalConnections(java.lang.String value);

	public java.lang.String getNonTransactionalConnections();
        
        public void setAllowNonComponentCallers(java.lang.String value);

	public java.lang.String getAllowNonComponentCallers();

}
