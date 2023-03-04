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

package org.netbeans.modules.j2ee.deployment.common.api;

/**
 * This interface is used to communicate information about changes in
 * ejb remote interfaces and deployment descriptors in support of
 * incremental and iterative deployment.  An instance of this interface
 * is supplied by a development module and passed to a plugin module
 * during an incremental deploy.  The plugin can use this information
 * to potentially skip steps in the deploy process.  For example, if
 * no ejbs have changed, the stub generation step may be skipped.
 * @author  George Finklang
 */
public interface EjbChangeDescriptor {

   /* @returns true if signatures of the remote interfaces of any ejbs have
    * changed, or if any part of the deployment descriptor that relates to
    * ejb code generation has changed. */
   public boolean ejbsChanged();
   
   /* @returns String array of the names of the ejbs that have changed */
   public String[] getChangedEjbs();
}
