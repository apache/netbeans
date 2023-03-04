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

package org.openide.loaders;


import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.ConnectionCookie;
import org.openide.nodes.Node;

/** Support implementing ConnectionCookie, that stores
* listeners in extended attributes of associated entry.
*
* @author Jaroslav Tulach, Petr Hamernik
 * @deprecated Should no longer be used.
*/
@Deprecated
public class ConnectionSupport extends Object implements ConnectionCookie {
    /** extended attribute to store (ArrayList of Type and Node.Handle) */
    private static final String EA_LISTENERS = "EA-OpenIDE-Connection"; // NOI18N

    /** entry to work on */
    private MultiDataObject.Entry entry;
    /** array of types */
    private ConnectionCookie.Type[] types;
    /** cached the return value for getTypes method */
    private Set<ConnectionCookie.Type> typesSet;

    /** table of listeners for non-persistent types. Array of Pairs.
    */
    private LinkedList<Pair> listeners;

    /** Creates new connection support for given file entry.
    * @param entry entry to store listener to its extended attributes
    * @param types a list of event types to support
    */
    public ConnectionSupport (MultiDataObject.Entry entry, ConnectionCookie.Type[] types) {
        this.entry = entry;
        this.types = types;
        // #45750 - init list for CC.T which are not persistent
        listeners = new LinkedList<Pair>();
    }

    /** Attaches new node to listen to events produced by this
    * event. The type must be one of event types supported by this
    * cookie and the listener should have ConnectionCookie.Listener cookie
    * attached so it can be notified when event of requested type occurs.
    *
    * @param type the type of event, must be supported by the cookie
    * @param listener the node that should be notified
    *
    * @exception InvalidObjectException if the type is not supported by the cookie (subclass of IOException)
    * @exception IOException if the type is persistent and the listener does not
    *    have serializable handle (listener.getHandle () is null or its serialization
    *    throws an exception)
    */
    public synchronized void register (ConnectionCookie.Type type, Node listener) throws IOException {
        // test if the file is supported, if not throws exception
        testSupported (type);

        boolean persistent = type.isPersistent ();
        LinkedList<Pair> list;

        if (persistent) {
            list = (LinkedList<Pair>)entry.getFile ().getAttribute (EA_LISTENERS);
        } else {
            list = listeners;
        }

        if (list == null) {
            // empty list => create new
            list = new LinkedList<Pair> ();
        }

        //    System.out.println("======================================== ADD:"+entry.getFile().getName()); // NOI18N
        //    System.out.println(this);
        //    System.out.println("size:"+list.size()); // NOI18N

        Iterator<Pair> it = list.iterator ();
        while (it.hasNext ()) {
            Pair pair = it.next ();
            //      System.out.println("test:"+pair.getType()); // NOI18N
            if (type.equals (pair.getType ())) {
                Node n;
                try {
                    n = pair.getNode ();
                    //          System.out.println("  node:"+n); // NOI18N
                } catch (IOException e) {
                    // node that cannot produce handle => remove it
                    Logger.getLogger(ConnectionSupport.class.getName()).log(Level.WARNING, null, e);
                    it.remove ();
                    // go on
                    continue;
                }
                //        System.out.println("  compare with:"+listener); // NOI18N
                if (n.equals (listener)) {
                    // we found our node - it is already in the list.
                    //          System.out.println("the listener found - remove it."); // NOI18N
                    it.remove();
                    continue;
                }
                else {
                    //          System.out.println("  nene"); // NOI18N
                }
            }
        }
        list.add (persistent ? new Pair (type, listener.getHandle ()) : new Pair (type, listener));

        //    System.out.println("after add:"+list.size()); // NOI18N

        if (persistent) {
            // save can throw IOException
            entry.getFile ().setAttribute (EA_LISTENERS, list);
        }

    }

    /** Unregisters an listener.
    * @param type type of event to unregister the listener from listening to
    * @param listener to unregister
    * @exception IOException if there is I/O operation error when the removing
    *   the listener from persistent storage
    */
    public synchronized void unregister (ConnectionCookie.Type type, Node listener) throws IOException {
        // test if the file is supported, if not throws exception
        testSupported (type);

        boolean persistent = type.isPersistent ();
        LinkedList list;

        if (persistent) {
            list = (LinkedList)entry.getFile ().getAttribute (EA_LISTENERS);
        } else {
            list = listeners;
        }

        if (list == null) {
            // empty list => no work
            return;
        }

        //    System.out.println("======================================== REMOVE:"+entry.getFile().getName()); // NOI18N
        //    System.out.println(this);
        //  System.out.println("size:"+list.size()); // NOI18N

        Iterator it = list.iterator ();
        while (it.hasNext ()) {
            Pair pair = (Pair)it.next ();

            if (type.equals (pair.getType ())) {
                Node n;
                try {
                    n = pair.getNode ();
                } catch (IOException e) {
                    // node that cannot produce handle => remove it
                    it.remove ();
                    // go on
                    continue;
                }
                if (n.equals (listener)) {
                    // we found our node
                    it.remove ();
                    // break the cycle but save if necessary

                    continue;
                }
            }
        }

        //System.out.println("after remove:"+list.size()); // NOI18N

        if (persistent) {
            // save can throw IOException
            entry.getFile ().setAttribute (EA_LISTENERS, list);
        }
    }

    /** Unmutable set of types supported by this connection source.
    * @return a set of Type objects
    */
    public Set<ConnectionCookie.Type> getTypes () {
        if (typesSet == null)
            typesSet = Collections.unmodifiableSet (new HashSet<ConnectionCookie.Type> (Arrays.asList (types)));
        return typesSet;
    }

    /** Get the list of all registered types in every (persistent
    * or not persistent) connections.
    *
    * @return the list of ConnectionCookie.Type objects
    */
    public List<ConnectionCookie.Type> getRegisteredTypes() {
        LinkedList<ConnectionCookie.Type> typesList = new LinkedList<ConnectionCookie.Type>();

        LinkedList<Pair> list = listeners;
        for (int i = 0; i <= 1; i++) {
            if (i == 1)
                list = (LinkedList<Pair>)entry.getFile ().getAttribute (EA_LISTENERS);

            if (list == null)
                continue;

            for (Pair p: list) {
                typesList.add(p.getType());
            }
        }

        return typesList;
    }

    /** Fires info for all listeners of given type.
    * @param ev the event
    */
    public void fireEvent (ConnectionCookie.Event ev) {
        LinkedList<Pair> list;
        ConnectionCookie.Type type;
        boolean persistent;
        
        synchronized (this) {
            type = ev.getType ();

            persistent = type.isPersistent ();
            if (persistent) {
                list = (LinkedList<Pair>)entry.getFile ().getAttribute (EA_LISTENERS);
            } else {
                list = listeners;
            }

            if (list == null) return;
         
            list = (LinkedList<Pair>)list.clone ();
        }

        int size = list.size ();

        Iterator<Pair> it = list.iterator ();
        while (it.hasNext ()) {
            Pair pair = it.next ();

            if (pair.getType ().overlaps(ev.getType())) {
                try {
                    ConnectionCookie.Listener l = pair.getNode().getCookie(ConnectionCookie.Listener.class);
                    if (l != null) {
                        try {
                            l.notify (ev);
                        } catch (IllegalArgumentException e) {
                            it.remove ();
                        } catch (ClassCastException e) {			
                            it.remove ();
                        }
		    }
                } catch (IOException e) {
                    it.remove ();
                }
            }
        }
	
	// if something in the list has changed, save it.
        if (persistent && list.size() != size) {
            // save can throw IOException
            try {
                entry.getFile ().setAttribute (EA_LISTENERS, list);
            } catch (IOException e) {
                // ignore never mind
            }
        }
    }

    /** Obtains a set of all listeners for given type.
    * @param type type of events to test
    * @return unmutable set of all listeners (Node) for a type
    */
    public synchronized java.util.Set/*<Node>*/ listenersFor (ConnectionCookie.Type type) {
        LinkedList<Pair> list;

        if (type.isPersistent ()) {
            list = (LinkedList<Pair>)entry.getFile ().getAttribute (EA_LISTENERS);
        } else {
            list = listeners;
        }

        if (list == null) return Collections.emptySet();

        Iterator<Pair> it = list.iterator ();
        HashSet<Node> set = new HashSet<Node> (7);

        while (it.hasNext ()) {
            Pair pair = it.next();
            if (type.overlaps(pair.getType ())) {
                try {
                    set.add (pair.getNode ());
                } catch (IOException e) {
                    // ignore the exception
                }
            }
        }

        return set;
    }


    /** Test if the type is supported.
    * @param t type
    * @exception InvalidObjectException if type is not valid
    */
    private void testSupported (ConnectionCookie.Type t) throws InvalidObjectException {
        for (int i = 0; i < types.length; i++) {
            if (t.overlaps(types[i])) {
                return;
            }
        }
        throw new InvalidObjectException (t.toString ());
    }

    /** A pair of type of event and a handle to it.
    */
    private static final class Pair extends Object implements java.io.Serializable {
        /** type of the listener */
        private ConnectionCookie.Type type;
        /** the node or the handle to a node */
        private Object value;

        static final long serialVersionUID =387180886175136728L;
        /** @param t the type of the event
        * @param n the listener
        */
        public Pair (ConnectionCookie.Type t, Node n) {
            type = t;
            value = n;
        }

        /** @param t the type of the event
        * @param h the listener's handle
        * @exception IOException if handle is null
        */
        public Pair (ConnectionCookie.Type t, Node.Handle h) throws IOException {

            if (h == null) throw new IOException ();

            type = t;
            value = h;
        }

        /** Getter of the type.
        */
        public ConnectionCookie.Type getType () {
            return type;
        }

        /** Getter of the listener.
        * @return listener's node
        * @exception IOException if the handle is not able to create a node
        */
        public Node getNode () throws IOException {
            return value instanceof Node ? (Node)value : ((Node.Handle)value).getNode ();
        }
    }
}
