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
package mypackage;

/**
 *
 * @author luke
 */
public class MyBean {
    
    /** Creates a new instance of MyBean */
    public MyBean() {
    }
    
    private String name;
    private boolean noErrors;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String doSome(){
        return null;
    }

    public boolean isNoErrors(){
        return noErrors;
    }

    public void setIsNoErrors(boolean newValue){
        noErrors = newValue;
    }
}
