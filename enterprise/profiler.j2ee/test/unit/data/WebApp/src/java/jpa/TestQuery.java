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
package jpa;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.TemporalType;


/**
 * @author ads
 *
 */
public class TestQuery implements Query {

    /* (non-Javadoc)
     * @see javax.persistence.Query#executeUpdate()
     */
    @Override
    public int executeUpdate() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getResultList()
     */
    @Override
    public List getResultList() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getSingleResult()
     */
    @Override
    public Object getSingleResult() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setFirstResult(int)
     */
    @Override
    public Query setFirstResult( int arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setFlushMode(javax.persistence.FlushModeType)
     */
    @Override
    public Query setFlushMode( FlushModeType arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setHint(java.lang.String, java.lang.Object)
     */
    @Override
    public Query setHint( String arg0, Object arg1 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setMaxResults(int)
     */
    @Override
    public Query setMaxResults( int arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(java.lang.String, java.lang.Object)
     */
    @Override
    public Query setParameter( String arg0, Object arg1 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(int, java.lang.Object)
     */
    @Override
    public Query setParameter( int arg0, Object arg1 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(java.lang.String, java.util.Date, javax.persistence.TemporalType)
     */
    @Override
    public Query setParameter( String arg0, Date arg1, TemporalType arg2 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(java.lang.String, java.util.Calendar, javax.persistence.TemporalType)
     */
    @Override
    public Query setParameter( String arg0, Calendar arg1, TemporalType arg2 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(int, java.util.Date, javax.persistence.TemporalType)
     */
    @Override
    public Query setParameter( int arg0, Date arg1, TemporalType arg2 ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(int, java.util.Calendar, javax.persistence.TemporalType)
     */
    @Override
    public Query setParameter( int arg0, Calendar arg1, TemporalType arg2 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public void method(){
        
    }
}
