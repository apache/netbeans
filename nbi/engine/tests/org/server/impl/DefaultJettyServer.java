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

package org.server.impl;

//import java.io.IOException;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.*;
//import org.mortbay.jetty.Handler;
//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.handler.ContextHandler;
//import org.mortbay.jetty.handler.DefaultHandler;
//import org.mortbay.jetty.handler.ResourceHandler;
//import org.mortbay.jetty.servlet.Context;
//import org.mortbay.jetty.servlet.ServletHandler;
//import org.mortbay.jetty.servlet.ServletHolder;
//import org.server.*;

/**
 *
 * @author Danila_Dugurov
 */
public class DefaultJettyServer /*extends AbstractServer*/ {
  
//  private Server httpServer;
  
  public DefaultJettyServer(String testDataPath, int serverPort) {
//    super(testDataPath, serverPort);
  }
  
  public void start() throws Exception {
//    if (httpServer == null) httpServer = new Server(serverPort);
//    if (httpServer.isRunning()) return;
//    final ResourceHandler handler = new ResourceHandler();
//    handler.setResourceBase(testDataPath);
////    Context redirect = new Context(httpServer,"/",Context.SESSIONS);
//  //  redirect.addServlet(new ServletHolder(new RedirectServlet()), "/redirect/*");
//    ServletHandler redirectServlet = new ServletHandler();
//    redirectServlet.addServletWithMapping(RedirectServlet.class, "/redirect/*");
//    httpServer.addHandler(handler);
//    httpServer.addHandler(redirectServlet);
//    httpServer.start();
  }
  
  public void stop() throws Exception {
//    if (httpServer == null && httpServer.isStopped()) return;
//    httpServer.stop();
  }
}
