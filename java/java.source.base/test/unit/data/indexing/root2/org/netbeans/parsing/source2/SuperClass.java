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
package org.netbeans.parsing.source2;

import javax.swing.table.DefaultTableModel;

/**
 *
 * 
 */
public class SuperClass extends DefaultTableModel implements Runnable {
   
    /**
     * public ctor
     */
    public SuperClass() {
        Integer i;
        
    }

    /**
     * public method 
     */
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * package private inner class
     */
    class Inner {
        
    }
    
    /**
     * private field
     */
    private String field;
    
    /**
     * protected field
     */    
    protected  int field2;
    
    /**
     * package private enum
     * containt 3 constants, privat  ctor (implicit) and two static methods(implicit)
     */
    enum Color {R,G,B};

}
