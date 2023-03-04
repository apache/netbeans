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


/*
 * TestBean.java
 *
 * Created on 22. leden 2004, 14:13
 */

package test;

/**
 *
 * @author  lm97939
 */
public class TestBean {
    
    /**
     * Holds value of property stringProperty.
     */
    private String stringProperty;
    
    /**
     * Holds value of property intProperty.
     */
    private int intProperty;
    
    /** Creates a new instance of TestBean */
    public TestBean() {
    }
    
    /**
     * Getter for property stringProperty.
     * @return Value of property stringProperty.
     */
    public String getStringProperty() {
        return this.stringProperty;
    }
    
    /**
     * Setter for property stringProperty.
     * @param stringProperty New value of property stringProperty.
     */
    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }
    
    /**
     * Getter for property intProperty.
     * @return Value of property intProperty.
     */
    public int getIntProperty() {
        return this.intProperty;
    }
    
    /**
     * Setter for property intProperty.
     * @param intProperty New value of property intProperty.
     */
    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }
    
    public int add(int x) {
        return getIntProperty() + x ;
    }
    
}
