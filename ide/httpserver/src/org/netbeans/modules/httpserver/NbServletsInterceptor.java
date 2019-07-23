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

package org.netbeans.modules.httpserver;

import org.apache.tomcat.core.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.util.Exceptions;

/**
 * @author costin@dnt.ro
 */
public class NbServletsInterceptor extends BaseInterceptor {

    public NbServletsInterceptor() {
    }

    private ServletWrapper addServlet( Context ctx, String name, String classN )
	throws TomcatException {
	ServletWrapper sw=new ServletWrapper();
	sw.setContext(ctx);
	sw.setServletName( name );
	sw.setServletClass( classN);
	ctx.addServlet( sw );
	sw.setLoadOnStartUp(0);
	return sw;
    }
    
    private void addNbServlets( Context ctx ) throws TomcatException {
        HttpServerSettings op = HttpServerSettings.getDefault();
        ServletWrapper sw;
        
	sw=addServlet( ctx, "WrapperServlet", "org.netbeans.modules.httpserver.WrapperServlet");        // NOI18N
	ctx.addServletMapping(op.getWrapperBaseURL () + "*", "WrapperServlet");                         // NOI18N
    }
    
    public void contextInit(Context ctx) throws TomcatException {
	if( ctx.getDebug() > 0 ) ctx.log("NbServletsInterceptor - init  " + ctx.getPath() + " " + ctx.getDocBase() );  // NOI18N
	ContextManager cm=ctx.getContextManager();
	
	try {
	    // Default init
	    addNbServlets( ctx );

	} catch (Exception e) {
            Exceptions.attachMessage(e, "NbServletsInterceptor failed"); // NOI18N
	    Logger.getLogger("global").log(Level.INFO, null, e);
	}

    }

}

