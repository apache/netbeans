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
package org.netbeans.modules.cloud.oracle.actions;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jhorvath
 */
public class AddDbConnectionToVaultTest {
    
    public AddDbConnectionToVaultTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of extractDatasourceName method, of class AddDbConnectionToVault.
     */
    @Test
    public void datasourceName() {
        String input = "DATASOURCES_DEFAULT_USERNAME";
        String ds = AddDbConnectionToVault.extractDatasourceName(input);
        assertEquals("DEFAULT", ds);
        
        input = "DATASOURCES_DEF3_USERNAME";
        ds = AddDbConnectionToVault.extractDatasourceName(input);
        assertEquals("DEF3", ds);
    }
    
    @Test
    public void testConfigMap() {
        String cm = "apiVersion: v1\n" +
            "kind: ConfigMap\n" +
            "metadata:\n" +
            "  name: demo-adb-vault\n" +
            "data:\n" +
            "  bootstrap-oraclecloud.properties: |\n" +
            "    oci.config.instance-principal.enabled=false\n" +
            "    micronaut.config-client.enabled=fase\n" +
            "    oci.vault.config.enabled=false\n" +
            "    oci.vault.vaults[0].ocid=xxxx\n" +
            "    oci.vault.vaults[0].compartment-ocid=xxx\n" +
            "  application-oraclecloud.properties: |\n" +
            "    a=b";
        String expected = "apiVersion: v1\n" +
            "kind: ConfigMap\n" +
            "metadata:\n" +
            "  name: demo-adb-vault\n" +
            "data:\n" +
            "  bootstrap-oraclecloud.properties: |\n" +
            "    oci.config.instance-principal.enabled=true\n" +
            "    micronaut.config-client.enabled=true\n" +
            "    oci.vault.config.enabled=true\n" +
            "    oci.vault.vaults[0].ocid=cde\n" +
            "    oci.vault.vaults[0].compartment-ocid=abc\n" +
            "  application-oraclecloud.properties: |\n" +
            "    a=b\n" +
            "    datasources.default.dialect=ORACLE\n" +
            "    datasources.default.ocid=${DATASOURCES_DEFAULT_OCID}\n" +
            "    datasources.default.walletPassword=${DATASOURCES_DEFAULT_WALLET_PASSWORD}\n" +
            "    datasources.default.username=${DATASOURCES_DEFAULT_USERNAME}\n" +
            "    datasources.default.password=${DATASOURCES_DEFAULT_PASSWORD}\n";
        String result = AddDbConnectionToVault.updateProperties(cm, "abc", "cde", "DEFAULT");
        assertEquals(expected, result);
    }
    
    @Test
    public void testConfigMap1() {
        String cm = "apiVersion: v1\n" +
            "kind: ConfigMap\n" +
            "metadata:\n" +
            "  name: demo-adb-vault\n" +
            "data:\n" +
            "  bootstrap-oraclecloud.properties: |\n" +
            "    # placeholder\n" +
            "  application-oraclecloud.properties: |\n" +
            "    a=b";
        String expected = "apiVersion: v1\n" +
            "kind: ConfigMap\n" +
            "metadata:\n" +
            "  name: demo-adb-vault\n" +
            "data:\n" +
            "  bootstrap-oraclecloud.properties: |\n" +
            "    # placeholder\n" +
            "    oci.config.instance-principal.enabled=true\n" +
            "    micronaut.config-client.enabled=true\n" +
            "    oci.vault.config.enabled=true\n" +
            "    oci.vault.vaults[0].ocid=cde\n" +
            "    oci.vault.vaults[0].compartment-ocid=abc\n" +
            "  application-oraclecloud.properties: |\n" +
            "    a=b\n" +
            "    datasources.default.dialect=ORACLE\n" +
            "    datasources.default.ocid=${DATASOURCES_ABC_OCID}\n" +
            "    datasources.default.walletPassword=${DATASOURCES_ABC_WALLET_PASSWORD}\n" +
            "    datasources.default.username=${DATASOURCES_ABC_USERNAME}\n" +
            "    datasources.default.password=${DATASOURCES_ABC_PASSWORD}\n";
        String result = AddDbConnectionToVault.updateProperties(cm, "abc", "cde", "ABC");
        assertEquals(expected, result);
    }
}
