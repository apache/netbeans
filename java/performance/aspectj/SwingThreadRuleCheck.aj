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

package ajswing;

import javax.swing.JComponent;
import java.awt.EventQueue;
import java.awt.Component;
import java.awt.Container;

aspect SwingThreadRuleCheck
{
    pointcut swingMethods(JComponent comp) : target(comp) && execution(public * javax.swing..*(..));
    pointcut threadsafeMethods() :
        execution(* *..repaint(..))
        || execution(* *..revalidate(..))
        || execution(* *..get*(..))
        || execution(* *..is*(..))
        || execution(* *..putClientProperty(..))
        || execution(* *..reshape(..))
        || execution(* *..addNotify())
        || execution(* *..setVisible(..))
        || execution(* *..add*Listener(..))
        || execution(* *..remove*Listener(..));

    before(JComponent comp) : ! threadsafeMethods()
                              && swingMethods(comp)
                              && ! if (EventQueue.isDispatchThread())
                              && if (comp.isShowing())
                                  && ! cflow(call(public void java.awt.Window.pack()))
    {
        if (thisJoinPointStaticPart.getSignature().getDeclaringType().getName().startsWith("javax.swing.")) {
            System.err.println("SwingThreadRuleCheck: Swing Single-Thread rule is being violated");
            Thread.dumpStack();
        }
    }
}
