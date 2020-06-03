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
package org.netbeans.modules.cnd.testrunner.ui;

//import java.awt.event.ActionEvent;
//import java.util.List;
//import javax.swing.AbstractAction;
//import org.netbeans.modules.gsf.testrunner.Manager;
//import org.netbeans.modules.gsf.testrunner.TestSession;
////import org.netbeans.modules.gsf.testrunner.output.OutputLine;
//
///**
// *
// */
//final class DisplayOutputForNodeAction extends AbstractAction {
//
//    private final List<OutputLine> output;
//    private final TestSession session;
//
//    public DisplayOutputForNodeAction(List<OutputLine> output, TestSession session) {
//        this.output = output;
//        this.session = session;
//    }
//
//
//    public Object getValue(String key) {
//        if (NAME.equals(key)) {
//            return "display";
//        }
//        return super.getValue(key);
//    }
//
//    public void actionPerformed(ActionEvent e) {
//        Manager manager = Manager.getInstance();
//        for (OutputLine ol : output) {
//            manager.displayOutput(session, ol.getLine(), ol.isError());
//        }
//    }
//}
