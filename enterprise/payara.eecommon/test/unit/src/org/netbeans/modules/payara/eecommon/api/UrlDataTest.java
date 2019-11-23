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

package org.netbeans.modules.payara.eecommon.api;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Williams
 */
public class UrlDataTest {

    public UrlDataTest() {
    }

    private static String [] urls = {
            "jdbc:derby://localhost:1527/travel",
            "jdbc:derby://localhost:1527/travel;create=true",
            "jdbc:sun:sqlserver://localhost:1433;databaseName=sampledb",
            "jdbc:sun:sqlserver://localhost:1433;databaseName=sampledb;create=true;foo=bar;bar=foo",
            "jdbc:sun:sqlserver://localhost:1433;create=true;databaseName=sampledb;foo=bar;bar=foo",
            "jdbc:sqlserver://localhost:1433",
            "jdbc:sqlserver://localhost:1433/sampledb",
            "jdbc:sqlserver://localhost\\instanceName:1433",
            "jdbc:sqlserver://localhost\\instanceName:1433/sampledb",
            "jdbc:sun:oracle://localhost:1521;SID=sampledb",
            "jdbc:oracle:thin:@localhost:1521:sampledb",
            "jdbc:oracle:thin:@localhost:sampledb",
            "jdbc:oracle:thin:@localhost:1521",
            "jdbc:mysql://localhost:3306/baza1250?autoReconnect=true&characterEncoding=cp1250&characterSetResults=cp1250",
            "jdbc:postgresql://localhost:5432/sampledb",
            "jdbc:weblogic:mssqlserver4:sampledb@localhost:1433",
            "jdbc:informix-sqli://localhost:1530/sampledb:INFORMIXSERVER=informixinstancename",
            "jdbc:datadirect:informix://localhost:1530;informixServer=informixinstancename;databaseName=sampledb",
            "jdbc:as400://9.88.24.163",
            "jdbc:as400://myiSeries;database name=IASP1"
    };
    
    /**
     * Test of database URL parsing
     */
    @Test
    public void testUrlParser() {
        for(String url: urls) {
            System.out.println("Parsing: " + url);
            UrlData data = new UrlData(url);
            assertEquals("Parsing " + url + " failed.", url, data.constructUrl());
        }
    }

}
