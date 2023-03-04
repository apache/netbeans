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
 /* PageFlowAcceptProvider.java
 *
 * Created on March 5, 2007, 1:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author joelle
 */
public class PageFlowAcceptProvider implements AcceptProvider {
    
    /** Creates a new instance of PageFlowAcceptProvider */
    public PageFlowAcceptProvider() {
    }

    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
        
        System.out.print("\nPageFlowAcceptProvider: IS ACCEPTABLE HAS BEEN CALLED.");
        System.out.println("Widget: " + widget);
        System.out.println("Point: " + point);
        System.out.println("Transferable: " + transferable);
        DataFlavor[] dfs = transferable.getTransferDataFlavors();
        for( DataFlavor flavor: dfs){
            System.out.println("Data Flavor: " + flavor);
        }
        return ConnectorState.REJECT_AND_STOP;
    }

    public void accept(Widget widget, Point point, Transferable transferable) {
    }
    
}
