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
package org.netbeans.modules.python.debugger.spi;

/**
 * Best effort for collecting Python Debug info for netbeans 
 * debugger
 */
public class PythonEvent 
{
  
  public final static int UNDEFINED = -1  ;
  public final static int START_SESSION = 0  ;
  public final static int STOP_SESSION  = 1  ;
  public final static int START_RUN  = 2  ;
  
  private int _evtType = UNDEFINED ;           
  private PythonSession _session ; 
  
  /** Creates a new instance of PythonEvent */
  public PythonEvent( int evtType , PythonSession session ) 
  { 
    _evtType = evtType  ; 
    _session = session  ; 
  }

  public boolean isJython()
  { return _session.isJython() ; }
  
  public PythonSession getSession() 
  { return _session ; }
  public int getEvtType() 
  { return _evtType ; }
  
  
}
