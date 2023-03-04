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

package org.netbeans.modules.xml.multiview;

 /** Error.java
 *
 * Created on November 20, 2004, 12:27 PM
 * @author mkuchtiak
 */
public class Error {

    public static final int TYPE_FATAL=0;
    public static final int TYPE_WARNING=1;

    public static final int ERROR_MESSAGE=0;
    public static final int WARNING_MESSAGE=1;
    public static final int MISSING_VALUE_MESSAGE=2;
    public static final int DUPLICATE_VALUE_MESSAGE=3;

    private int errorType;
    private int severityLevel;
    private String errorMessage;
    private javax.swing.JComponent focusableComponent;
    private ErrorLocation errorLocation;
    
    /*
    public Error(int errorType, String errorMessage, javax.swing.JComponent focusableComponent) {
        this(TYPE_WARNING, errorType, errorMessage, focusableComponent);
    }

    public Error(int severityLevel, int errorType, String errorMessage, javax.swing.JComponent focusableComponent) {
        this.severityLevel=severityLevel;
        this.errorType=errorType;
        this.errorMessage=errorMessage;
        this.focusableComponent=focusableComponent;
    }
    */
    public Error(int errorType, String errorMessage, javax.swing.JComponent focusableComponent) {
        this(TYPE_WARNING ,errorType, errorMessage, focusableComponent);
    }
        
    public Error(int severityLevel, int errorType, String errorMessage, javax.swing.JComponent focusableComponent) {
        this.severityLevel=severityLevel;
        this.errorType=errorType;
        this.errorMessage=errorMessage;
        this.focusableComponent=focusableComponent;
    }  
    
    public Error(int errorType, String errorMessage, ErrorLocation errorLocation) {
        this(TYPE_WARNING,errorType, errorMessage, errorLocation);
    }   
    
    public Error(int severityLevel, int errorType, String errorMessage, ErrorLocation errorLocation) {
        this.severityLevel=severityLevel;
        this.errorType=errorType;
        this.errorMessage=errorMessage;
        this.errorLocation=errorLocation;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }
    
    public int getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public javax.swing.JComponent getFocusableComponent() {
        return focusableComponent;
    }

    public ErrorLocation getErrorLocation() {
        return errorLocation;
    }
    
    public boolean isEditError() {
        return (focusableComponent!=null);
    }
    
    /** Object that will enable to identify the place in section view where the error 
     * should be fixed. This is intended to use in SectionView:validateView() method.
     */
    public static class ErrorLocation {
        private Object key;
        private String componentId;
        
        public ErrorLocation (Object key, String componentId) {
            this.key=key;
            this.componentId=componentId;
        }
        
        public Object getKey() {
            return key;
        }
        public String getComponentId() {
            return componentId;
        }
    }
    
}
