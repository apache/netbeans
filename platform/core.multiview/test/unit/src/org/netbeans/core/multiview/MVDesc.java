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

package org.netbeans.core.multiview;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  mkleint
 */
public class MVDesc implements ContextAwareDescription {

        protected String name;
        protected Image img;
        public transient MultiViewElement el;
        protected int type;
        
        
        public MVDesc() {
            
        }
        
        public MVDesc(String name, Image img, int persType, MultiViewElement element) {
            el = element;
            this.name = name;
            this.img = img;
            type = persType;
        }
        
        public MultiViewElement createElement() {
            if (el == null) {
                // for persistence.. elem is transient..
                el = new MVElem();
            }
            return el;
        }
        
        public String getDisplayName() {
            return name;
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return new HelpCtx(name);
        }
        
        public java.awt.Image getIcon() {
            return img;
        }
        
        public int getPersistenceType() {
            return type;
        }
        
        public String preferredID() {
            return name;
        }

        @Override
        public ContextAwareDescription createContextAwareDescription(Lookup context, boolean isSplitDescription) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isSplitDescription() {
            return false;
        }
        
    }
