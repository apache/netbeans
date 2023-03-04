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
package test

import groovy.sql.Sql
import org.xml.sax.SAXParseException

/**
 *
 * @author lukas
 */
class Utils {
    private Utils(){}

    private static Sql db

    static {
        db = Sql.newInstance("jdbc:derby://localhost:1527/sample", "app", "app", "org.apache.derby.jdbc.ClientDriver")
    }

    static void readXml(String text) throws SAXParseException {
        new XmlParser().parseText(text)
    }

    static String readFile(String relPath) {
        Utils.class.getResource(relPath).getText()
    }

    static int getCreditLimit(int customerID) {
        def r = db.firstRow("select CREDIT_LIMIT from customer where CUSTOMER_ID=${customerID}")
        if (r) {
            return r['CREDIT_LIMIT']
        }
        return -1
    }
}

