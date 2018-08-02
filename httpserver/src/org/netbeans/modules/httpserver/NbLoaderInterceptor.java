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

import org.openide.util.Lookup;

/** Interceptor which tells the server about the classloader to be used by the
 *  server's servlet loaders.
 *
 * @author petr.jiricka@czech.sun.com
 */
public class NbLoaderInterceptor extends BaseInterceptor {

    public NbLoaderInterceptor() {
    }

    private void setNbLoader( ContextManager cm ) throws TomcatException {
	ClassLoader cl = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        cm.setParentClassLoader(cl);
    }
    
    public void contextInit(Context ctx) throws TomcatException {
	if( ctx.getDebug() > 0 ) ctx.log("NbLoaderInterceptor - init"); // NOI18N
	ContextManager cm=ctx.getContextManager();
	
	try {
	    // Default init
	    setNbLoader( cm );

            // here we set values normally supplied by WebXmlReader that we want to remove
            // exluding welcome files and JspServlet so only session timeout is here
            ctx.setSessionTimeOut (30);
	} catch (Exception e) {
            String msg = "NbLoaderInterceptor failed";  // NOI18N
	    System.out.println(msg);
	}

    }

}

