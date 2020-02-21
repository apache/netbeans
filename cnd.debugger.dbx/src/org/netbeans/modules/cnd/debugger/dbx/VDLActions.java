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


package org.netbeans.modules.cnd.debugger.dbx;

/**
 * Interface of actions that the VDL parser will call back.
 * Watches and Variables wil prebably customize the actions to their own
 * taste.
 */
interface VDLActions {

    /**
     * Set expandability of  Variable
     */
    public void setLeaf(boolean l);
    
    /**
     * Set type of  Variable
     */
    public void setType(String type, String atype);

    /**
     * Indicate current var is a Java var
     */
    public void setJava(boolean e);
    
    /**
     * Indicate current value of var is modified
     */
    public void setDelta(boolean e);

    /**
     * Declare the begining of a new smplval
     */
    public void newSmplval(String name, String deref_name,
			   String type, String atype, boolean stat,
			   String value, String set_str, String deref_action,
			   String hint, boolean delta);

    /**
     * Declare the beginning of a new Aggregate
     */
    public void startAggregate(String name, String deref_name,
			       String type, String atype, boolean stat,
			       boolean delta, boolean isopen);

    /**
     * Declare the beginning of a new Aggregate
     */
    public void endAggregate();
};
