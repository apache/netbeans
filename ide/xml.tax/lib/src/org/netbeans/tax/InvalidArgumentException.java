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
package org.netbeans.tax;

/**
 * @author  Libor Kramolis
 * @version 0.1
 */
public class InvalidArgumentException extends TreeException {

    /** Serial Version UID */
    private static final long serialVersionUID =3768694309653946597L;

    /** */
    private Object argument;


    //
    // init
    //

    /**
     * @param arg violating value
     * @param msg exception context message (or null)
     */
    public InvalidArgumentException (Object arg, String msg) {
        super (msg == null ? "" : msg); // NOI18N
        
        argument = arg;
    }
    
    /**
     */
    public InvalidArgumentException (String msg, Exception exc) {
        super (msg, exc);
        
        argument = null;
    }
    
    /**
     */
    public InvalidArgumentException (Exception exc) {
        super (exc);
        
        argument = null;
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final Object getArgument () {
        return argument;
    }
    
    /**
     * Gives additioanl message about violating argument.
     */
    public final String getMessage () {
        String detail = ""; // NOI18N
        if (argument != null) {
            detail = " " + Util.THIS.getString ("PROP_violating_argument", argument);
        }
        
        return super.getMessage () + detail;
    }
    
}
