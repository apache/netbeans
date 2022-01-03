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

package org.netbeans.modules.cnd.modelutil;

import java.awt.Image;
import java.io.PrintStream;
import java.util.Enumeration;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Abstract base class for CsmNode.
 * Disadvantage of (previous version) of CsmNode is that it necessarily stores CsmObject.
 * AbstractNode just declares abstract method
 * CsmObject getCsmObject()
 *
 */
public abstract class AbstractCsmNode extends AbstractNode {

    public AbstractCsmNode(Children children, Lookup lookup) {
        super(children, lookup);
    }

    public AbstractCsmNode(Children children) {
        this(children, null);
    }

    public abstract CsmObject getCsmObject();

    @Override
    public Image getIcon(int param) {
        try {
            CsmObject csmObj = getCsmObject();
            if (csmObj != null) {
                return CsmImageLoader.getImage(csmObj);
            }
        } catch (AssertionError ex){
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return ImageUtilities.loadImage(CsmImageLoader.DEFAULT);
    }

    protected Image superGetIcon(int param) {
        return ImageUtilities.loadImage(CsmImageLoader.DEFAULT);
    }

    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }

    public void dump(PrintStream ps) {
	dump(new Tracer(ps));
    }

    protected void dump(Tracer tracer) {
	tracer.trace(this.getDisplayName());
	tracer.indent();
	for( Enumeration<Node> children = getChildren().nodes(); children.hasMoreElements(); ) {
	    Node child = children.nextElement();
	    if( child instanceof AbstractCsmNode ) {
		((AbstractCsmNode) child).dump(tracer);
	    }
	}
	tracer.unindent();
    }
}
