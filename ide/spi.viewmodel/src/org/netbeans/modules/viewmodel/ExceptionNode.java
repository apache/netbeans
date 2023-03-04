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

package org.netbeans.modules.viewmodel;

import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;
import java.lang.IllegalAccessException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author   Jan Jancura
 */
public class ExceptionNode extends AbstractNode {

    
    private Exception exception;
    
    // init ....................................................................

    /**
    * Creates root of call stack for given producer.
    */
    public ExceptionNode ( 
        Exception exception
    ) {
        super (
            Children.LEAF,
            Lookups.singleton (exception)
        );
        this.exception = exception;
        setIconBaseWithExtension ("org/openide/resources/actions/empty.gif");
    }
    
    public String getName () {
        return exception.getLocalizedMessage ();
    }
    
    public String getDisplayName () {
        return exception.getLocalizedMessage ();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (getClass ());
    }
}

