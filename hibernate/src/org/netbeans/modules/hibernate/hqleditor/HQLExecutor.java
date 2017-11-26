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
package org.netbeans.modules.hibernate.hqleditor;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.netbeans.api.progress.ProgressHandle;

/**
 * Executes HQL query.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HQLExecutor {

    /**
     * Executes given HQL query and returns the result.
     * @param hql the query
     * @param configFileObject hibernate configuration object.
     * @return HQLResult containing the execution result (including any errors).
     */
    public HQLResult execute(String hql, 
            SessionFactory sessionFactory,
            int maxRowCount,
            ProgressHandle ph) {
        HQLResult result = new HQLResult();
        try {
            ph.progress(60);
            
            Session session = sessionFactory.openSession();
            session.beginTransaction();

            ph.progress(70);
            
            Query query = session.createQuery(hql);
            query.setMaxResults(maxRowCount);

            hql = hql.trim();
            hql = hql.toUpperCase();

            if (hql.startsWith("UPDATE") || hql.startsWith("DELETE")) { //NOI18N
                result.setUpdateOrDeleteResult(query.executeUpdate());
            } else {
                result.setQueryResults(query.list());
            }
            
            session.getTransaction().commit();

        } catch (Exception e) {
            result.getExceptions().add(e);
        }
        return result;
    }
}
