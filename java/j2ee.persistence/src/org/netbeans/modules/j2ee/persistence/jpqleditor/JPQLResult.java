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

package org.netbeans.modules.j2ee.persistence.jpqleditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps JPQL execution results and errors.
 * 
 */
public class JPQLResult {
    private List results = new ArrayList();
    private int updateOrDeleteResult;
    private List<Throwable> exceptions = new ArrayList<>();
    private String sqlQuery;
    private String queryProblems;

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<Throwable> exceptions) {
        this.exceptions = exceptions;
    }

    public int getUpdateOrDeleteResult() {
        return updateOrDeleteResult;
    }

    public void setUpdateOrDeleteResult(int updateOrDeleteResult) {
        this.updateOrDeleteResult = updateOrDeleteResult;
    }
    
    public void setQueryResults(List results) {
        this.results = results;
    }

    public List getQueryResults() {
        return results;
    }

    /**
     * @return the sqlQuery
     */
    public String getSqlQuery() {
        return sqlQuery;
    }

    /**
     * @param sqlQuery the sqlQuery to set
     */
    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    void setQueryProblems(String queryProblems) {
        this.queryProblems = queryProblems;
    }
    
    public String getQueryProblems(){
        return queryProblems;
    }
}
