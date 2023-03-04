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

package org.netbeans.modules.xml.tax.parser;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.tax.io.TreeBuilder;
import org.netbeans.tax.io.TreeInputSource;
import org.netbeans.tax.io.TreeStreamBuilderErrorHandler;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Creates DOM-like structure called Tree.
 * <p>
 * The result structure consists from two layers:
 * declaration-layer-object <- instance-layer-object.getDecl() i.e.
 * similar to Class and Object.
 * <p>
 * It is believed that this representation is more suitable 
 * than pure DOM for development tool purposes. It may become
 * void as DOM3 introduces events, node identity, clones, merging etc.
 *
 * @author  Petr Kuzel
 * @author  Libor Kramolis
 * @version 2.0 delegation to implementations
 */
public class TreeStreamSource implements TreeInputSource {

    private static final String BUILDER_IMPL = "org.netbeans.tax.io.XNIBuilder"; // NOI18N
    
    
    /** */
    private InputSource inputSource;
    /** */
    private Class buildClass;
    /** */
    private ErrorHolder errorHolder;
        
    //
    // init
    //
    
    /** */
    public TreeStreamSource (Class buildClass, InputSource inputSource, URL url) {
	this.buildClass     = buildClass;
	this.inputSource    = inputSource;
        this.errorHolder    = new ErrorHolder();
    }


    //
    // from TreeInputSource
    //
    
    /**
     */
    public TreeBuilder getBuilder () {
        return getImplementation();
    }    
    
    
    //
    // itself
    //
    
    /**
     * Build new TreeDTD from given InputSource.
     */
/*    public synchronized TreeDTD buildDTDTree (InputSource in) throws IOException{
        TreeBuilderInterface builder = getImplementation (in, getSystemEntityResolver(), errorHolder);
        if (builder == null)
            return null;
        
        return builder.buildDTDTree (in, getSystemEntityResolver(), errorHolder);
    }*/
    

    /**
     * Create new TreeDocument from given input source.
     */
/*    public synchronized TreeDocument buildXMLTree (InputSource in) throws IOException {
        TreeBuilderInterface builder = getImplementation();        
        if (builder == null)
            return null;
        builder.setSource (in);
        builder.
        return builder.buildXMLTree(in, getSystemEntityResolver(), errorHolder);
    }*/
    
    /**
     * Get isolated implementation of tree builder
     */
    private TreeBuilder getImplementation () {
    
        ClassLoader loader;

        if (Boolean.getBoolean("netbeans.tax.use_private_xni_impl")) {  // NOI18N
            loader = ParserLoader.getInstance();
        if (loader == null) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Can not get loader."); // NOI18N

            return null;
        }
        } else {
            loader = TreeBuilder.class.getClassLoader();  // we need TAX module classloader which sees Xerces 2.4.0 library
        }
        
        Class impl_c = null;
        try {
            impl_c = loader.loadClass(BUILDER_IMPL);
        } catch (ClassNotFoundException ex) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("!!! TreeStreamSource.getImplementation", ex);

            return null;
        }                        
        
        try {
            java.lang.reflect.Constructor impl_const = impl_c.getConstructor (new Class[] { Class.class, InputSource.class, EntityResolver.class, TreeStreamBuilderErrorHandler.class });
            return (TreeBuilder) impl_const.newInstance (new Object[] { buildClass, inputSource, getSystemEntityResolver(), errorHolder });
//            return (TreeBuilder) impl_c.newInstance();
	} catch (java.lang.reflect.InvocationTargetException ex) {
	    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (ex);

	    return null;
        } catch (NoSuchMethodException ex) {
	    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (ex);
            
            return null;
        } catch (InstantiationException ex) {
	    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (ex);
            
            return null;
        } catch (IllegalAccessException ex) {
	    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (ex);
            
            return null;
        }
    
    }
    
    /** 
     * Return a user's catalog based resolver or null
     */
    private EntityResolver getSystemEntityResolver() {

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeStreamSource.getSystemEntityResolver:");

        UserCatalog catalog = UserCatalog.getDefault();

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    UserCatalog.getDefault() = " + catalog);

        EntityResolver resolver = (catalog == null ? null : catalog.getEntityResolver());

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    EntityResolver = " + resolver);
                
        if (resolver == null) return null;
        
        // wrap it to timeouted resolver
        resolver = new EntityResolverWrapper (resolver);
        
        return resolver;
    }

    // ~~~~~~~~~~~~~~~~~~~~~ ERROR REPORTING ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    //
    // class ErrorHolder
    //
    
    /**
     *
     */
    private class ErrorHolder implements TreeStreamBuilderErrorHandler {
          
        public void message (int type, org.xml.sax.SAXParseException e) {
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Builder ex", e);  // NOI18N

        }
        
    } // end: class ErrorHolder


    //
    // RESOLVERS
    //
    
    /**
     * class EmptyEntityResolver.
     */
    static class EmptyEntityResolver implements EntityResolver {

        public InputSource resolveEntity (String publicId, String systemId) throws SAXException, IOException {
            return null;
        }

    } // end: class EmptyEntityResolver

    
    /**
     * Timeout-ed resolver wrapping another one giving it
     * limited time to perform resolution.
     *
     * //??? It timeoutes just the resolution process, another timeouted
     * thing should be sockets actually used to download data, but how can
     * I get to socket to setSoTimeout()? See http://www.logicamente.com/sockets.html
     * how Borland JDK Http[Timeout]Handler is patched.
     */
    static class EntityResolverWrapper implements EntityResolver {
        /** */
        private final EntityResolver res;

        public EntityResolverWrapper(EntityResolver er) {
            if (er == null) throw new NullPointerException();
            res = er;
        }

        /*
         * It will block at maximum for timeout period. Then it throws IOException
         * indication that resolution timeouted.
         */
        public InputSource resolveEntity (final String publicId, final String systemId) throws SAXException, IOException {

            // parse catalogs explicitly eliminating timeout on first resolution
            // we use such IDs that there is not reason for I/O i.e. block (#19779)
            // [jglick] It is illegal to pass null for systemId! breaks e.g. contrib/docbook
            res.resolveEntity(null, "urn:nowhere"); // NOI18N

            final ErrorManager emgr = ErrorManager.getDefault();
            final InputSource MARK = new InputSource("mark"); // NOI18N

            try {

                final InputSource result[] = new InputSource[] {MARK};
                final SAXException sex[] = new SAXException[1];
                final IOException ioex[] = new IOException[1];

                // asynchronous thread body uses above fileds to comunicate
                // its result
                
                Runnable task = new Runnable() {
                    
                    public void run() {
                        
                        InputSource is = MARK;
                        try {
                            is = res.resolveEntity(publicId, systemId);
                        } catch (IOException _ioex) {
                            ioex[0] = _ioex;
                        } catch (SAXException _sex) {
                            sex[0] = _sex;
                        } finally {
                            synchronized (EntityResolverWrapper.this) {
                                if (is != MARK) result[0] = is;
                                EntityResolverWrapper.this.notify();
                            }                            
                        }
                    }
                };
                
                // use private thread per request
                // because there is no guarantee that it ever finishes
                
                Thread thread = new Thread(task, "Timeouted EntityResolver"); // NOI18N
                thread.setDaemon(true);
                thread.start();  

                // according to passed time change status line
                
                synchronized (this) {  //TRY IT
                    if (result[0] == MARK) {
                        wait(300);
                    }
                }

                if (result[0] == MARK) {
                    
                    StatusDisplayer.getDefault().setStatusText (Util.THIS.getString("MSG_resolving"));
                    
                    synchronized (this) { //TRY IT 2
                        int timeout = Integer.getInteger("netbeans.xml.resolver.timeout", 5000).intValue();  //??? expose to settings? // NOI18N
                        if (result[0] == MARK) {
                            wait(timeout);  // get notified by "Timeouted EntityResolver" thread
                        }
                    }
                }

                if (result[0] != MARK) {
                    return result[0];
                } else if (sex[0] != null) {
                    throw sex[0];
                } else if (ioex[0] != null) {
                    throw ioex[0];
                } else {
                    
                    // perform timeout procedure
                    
                    thread.interrupt();
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.setName("Zombie"); // NOI18N
                    
                    final IOException CANNOT_CONNECT =
                        new IOException("Resolution timeout \"" + systemId + "\" (" + publicId + ")"); // NOI18N
                    String pattern = Util.THIS.getString("MSG_cannot_connect");
                    Object[] params = new String[] {publicId, systemId};
                    String annotation = MessageFormat.format(pattern, params);
                    emgr.annotate(CANNOT_CONNECT, annotation);
                    
                    throw CANNOT_CONNECT;
                }
                
            } catch (InterruptedException iex) {
                
                // throw IOException annotated by InterruptedException reason
                
                final IOException INTERRUPTED =
                    new IOException("Resolution interrupted \"" + systemId + "\" (" + publicId + ")"); // NOI18N

                String pattern = Util.THIS.getString("MSG_interrupted");
                Object[] params = new String[] {publicId, systemId};
                String annotation = MessageFormat.format(pattern, params);
                emgr.annotate(INTERRUPTED, annotation);

                throw INTERRUPTED;
                
            } finally {
                StatusDisplayer.getDefault().setStatusText(""); // NOI18N
            }

        }
        
        public String toString() {
            return super.toString() + Util.THIS.getString ("PROP_wrapping") + res.toString();
        }
        
    } // end: class EntityResolverWrapper
        
} 
