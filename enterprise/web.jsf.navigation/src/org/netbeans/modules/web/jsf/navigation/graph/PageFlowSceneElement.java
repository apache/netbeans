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
package org.netbeans.modules.web.jsf.navigation.graph;
import java.awt.Image;
import java.io.IOException;
import org.openide.nodes.Node;

/*
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import org.openide.util.HelpCtx;
/**
 *
 * @author joelle
 */
public abstract class PageFlowSceneElement {
    private String name;


    public PageFlowSceneElement(){
    }

    public boolean equals(Object obj) {
        return (this == obj);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private boolean modifiable = true;
    public boolean isModifiable() {
        return modifiable;
    }
    public void setModifiable(boolean modifiable ){
        this.modifiable = modifiable;
    }

    public abstract Node getNode();
    public abstract HelpCtx getHelpCtx();
    public abstract void destroy() throws IOException;
    public abstract boolean canDestroy();
    public abstract boolean canRename();
    public abstract Image getIcon( int type );
}
