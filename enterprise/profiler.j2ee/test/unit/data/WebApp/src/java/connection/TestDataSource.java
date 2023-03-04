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
package connection;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;


/**
 * @author ads
 *
 */
public class TestDataSource implements DataSource {

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setLogWriter( PrintWriter arg0 ) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLoginTimeout( int arg0 ) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isWrapperFor( Class<?> iface ) throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap( Class<T> iface ) throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection( String arg0, String arg1 )
            throws SQLException
    {
        return null;
    }
    
    public void method(){
    }

}
