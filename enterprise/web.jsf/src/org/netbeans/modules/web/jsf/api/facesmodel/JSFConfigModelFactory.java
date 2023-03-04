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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelImpl;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;

/**
 *
 * @author Petr Pisl, ads
 */
public class JSFConfigModelFactory extends AbstractModelFactory<JSFConfigModel>{
    
    /** Creates a new instance of JSFConfigModelFactory */
    private JSFConfigModelFactory() {
    }
    
    public static JSFConfigModelFactory getInstance(){
        return INSTANCE;
    }
    
    protected JSFConfigModel createModel(ModelSource source) {
        return new JSFConfigModelImpl(source);
    }
    
    public JSFConfigModel getModel(ModelSource source) {
        return super.getModel(source);
    }
    
    private static final JSFConfigModelFactory INSTANCE = new JSFConfigModelFactory();
}
