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
package test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author js201828
 */
@Entity
@Table(name="CUSTOMER")
// completion for tables
/**
@Table(name=|
PRODUCT
@Table(name="PRODUCT"
*/
public class TestDBCompletion implements java.io.Serializable {

    @Id
    private Long id;
    
    /** Creates a new instance of TestDBCompletion */
    // completion for columns 
/**
@Column(name=|
CUSTOMER. ADDRESSLINE1
@Column(name="ADDRESSLINE1"
*/
 
    public TestDBCompletion() {
      }
}
