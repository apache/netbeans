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

package org.netbeans.modules.python.debugger;

import java.io.File ;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
import org.netbeans.modules.python.debugger.spi.PythonSession;
import org.openide.filesystems.FileObject;

/**
 * Node specialization for Python Sources debugging context
 */
public interface PythonSourceDebuggee
{
  /** Get the disk file for the python script.
  * @return the disk file, or null if none (but must be a file object)
  */
  File getFile ();
  /** Get the file object for the build script.
   * @return the file object, or null if none (but must be a disk file)
   */
  FileObject getFileObject ();
  
  /**
   bind a debug view object
  */
  public void setDebugView( JpyDbgView view ) ; 
  public JpyDbgView getDebugView() ; 
  
  /**
    execute current python shell action
  */ 
  //public void executePython() 
  //throws PythonDebugException ;
  
  /** set current python session */
  public void setSession( PythonSession pythonSession ) ; 
  public PythonSession getSession() ; 
  
}
