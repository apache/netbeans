/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.openide.cookies;

import java.util.Set;
import org.openide.nodes.Node;

import java.io.IOException;

import java.util.EventListener;


/** Cookie that allows connection between two objects. Also supporting
* persistent connections.
*
* @author Jaroslav Tulach
 * @deprecated Should no longer be used.
*/
@Deprecated
public interface ConnectionCookie extends Node.Cookie {
    /** Attaches new node to listen to events produced by this
    * event. The type must be one of event types supported by this
    * cookie and the listener should have ConnectionCookie.Listener cookie
    * attached so it can be notified when event of requested type occurs.
    *
    * @param type the type of event, must be supported by the cookie
    * @param listener the node that should be notified
    *
    * @exception InvalidObjectException if the type is not supported by the cookie
    * @exception IOException if the type is persistent and the listener does not
    *    have serializable handle (listener.getHandle () is null or its serialization
    *    throws an exception)
    */
    public void register(Type type, Node listener) throws IOException;

    /** Unregisters an listener.
    * @param type type of event to unregister the listener from listening to
    * @param listener to unregister
    * @exception IOException if there is I/O operation error when the removing
    *   the listener from persistent storage
    */
    public void unregister(Type type, Node listener) throws IOException;

    /** Immutable set of types supported by this connection source.
    * @return a set of types
    */
    public Set<? extends ConnectionCookie.Type> getTypes();

    /** Cookie that must be provided by a node that is willing to register
    * itself as a listener to a ConnectionCookie.
    */
    public interface Listener extends Node.Cookie, EventListener {
        /** Notifies that the an event happended.
        * @param ev event that describes the action
        * @exception IllegalArgumentException if the event is not of valid type, then the
        *    caller should call the listener no more
        * @exception ClassCastException if the event is not of valid type, then the
        *    caller should call the listener no more
        */
        public void notify(ConnectionCookie.Event ev) throws IllegalArgumentException, ClassCastException;
    }

    /** Interface describing cookie type of event a cookie can produce.
    */
    public interface Type extends java.io.Serializable {
        /** The class that is passed into the listener's <CODE>notify</CODE>
        * method when an event of this type is fired.
        *
        * @return event class
        */
        public Class<?> getEventClass();

        /** Getter whether the registration to this type of event  is persistent
        * or is valid only till the source disappears (the IDE shutdowns).
        */
        public boolean isPersistent();

        // Jesse, please improve the comment. [Petr]

        /** Test whether the specified type could be accepted by this type.
        * This method is similar to <CODE>equals(Object)</CODE> method,
        * so default implementation could be delegated to it.
        * @return <CODE>true</CODE> if type is similar to this type.
        */
        public boolean overlaps(Type type);
    }

    /** Event that is fired to listeners.
    */
    public class Event extends java.util.EventObject {
        static final long serialVersionUID = 7177610435688865839L;
        private Type type;

        /** @param n the node that produced the action
        * @param t type of the event
        */
        public Event(Node n, Type t) {
            super(n);
            type = t;
        }

        /** Getter for the node that produced the action.
        * The node can be used to obtain additional information like cookies, etc.
        * @return the node
        */
        public Node getNode() {
            return (Node) getSource();
        }

        /** Getter for the type of the event.
        * There can be more types of events and the listener can compare
        * if two events are of the same type by using type1.equals (type2)
        *
        * @return type of the event
        */
        public Type getType() {
            return type;
        }
    }
}
