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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.cnd.debugger.common2.debugger;

/**
   A class which represents a routing token/cookie enum, used
   for routing results from dbx to the client code that made
   the request.
   <p>
   The details:
   <p>
    In WorkShop we used the term "cookie" a lot in its traditional
meaning: opaque pointer, something a client may pass to the server,
and the server doesn't know or care what the "cookie" represents;
it then passes it back to the client who can interpret it.  In glue
it's actually called a routing token, which is a better description
of what it is.
<p>
In any case, in WorkShop it was just a <code>void *</code>. When
WorkShop sent a request to dbx, it would also sent along a cookie,
and when WorkShop heard back from dbx, the results would be accompanied
by the cookie sent with the request.  
<p>
This was used to route results appropriately. For example, a balloon
evaluation request from an editor would include a reference to the
editor object, so when the result came back we could find out which
editor was waiting for the result by just looking at the pointer.
<p>
In Java we don't have the same luxury - there are no <code>void
*</code>'s, so instead the routing token is not an <code>int</code>.
<p>
The question is, how do we use the int?
<p>
We have two needs from the routing token:
<ol>
 <li> Identify the component/subsystem in the IDE that this result
	needs to be routed to
 <li> That subsystem probably needs a further tag/id to manage
      the result. For example, if we've tried to modify a breakpoint,
      and we get an error message back, it would be helpful in addition
      to identifying this as a breakpoint problem, we had the relevant
      breakpoint id.
</ol>

<p>
To solve this problem, I propose that we use the 32 bit integer
as follows: the LOWEST 8 bits are used to identify the subsystem.
Note: I'm not talking about 8 possibilities here (one bit for each),
I'm talking about 256.   Then, the remaining 24 bits are used
as an ID for the subsystem.
<p>
Note that there any no big-endian/small-endian issues here. If you're
not convinced, read the fine print.
<h6><sup>
Cookies are opaque to anyone but the client. They have no business
mucking with it. The same Java process will both create and
interpret cookies - everybody else just passes it on, bit for bit.
</sup></h6>
 */


public final class RoutingToken {

    /**
     * Constant to mask-in the subsystem part of a routing token.
     */

    private final static int SUBSYSTEM_MASK = 0xFF;

    
    /**
     * Constant to shift up and unique portion of a routing token
     * so that it can be ORed with the subsystem portion of a routing
     * token to create a new routing token.
     */

    private final static int SUBSYSTEM_SHIFT = 8;


    private int id;
    

    /**
     * A unique token number that indicates that all tokens in
     * the same subsytem are to participate in the routing.
     * Make sure it cannot take on one of the nextTokenNumber values.
     */

    public static final int BROADCAST_TOKEN = 0;

    
    /**
     * Next available unique token number. Make sure it doesn't collide
     * with BROADCAST_TOKEN, though.
     */

    private static int nextTokenNumber = 1;

    
    /*
     * Create a routing token, given the full id value.
     * @param id The full id of the token.
     */

    private RoutingToken(int id) {
	this.id = id;
    }
    
    
    /**
     * Get the int equivalent of this routing token.
     * @return int The int equivalent of this routing token.
     */

    public final int getAsInt() {
        return id;
    }


    /**
     * Get a routing token which has the broadcast unique-id
     * within the subsystem of this routing token.
     * @return RoutingToken A routing token which has the broadcast
     * unique-id within the subsystem of this routing token.
     */

    public final RoutingToken getBroadcastRoutingToken() {
        return new RoutingToken((BROADCAST_TOKEN << SUBSYSTEM_SHIFT) |
				getSubsystemId());
    }

    
    /* 
     * Get the subsystem portion of the routing token.
     * @return int The subsystem portion of the routing token.
     */

    private final int getSubsystemId() {
        return id & SUBSYSTEM_MASK;
    }

    
    /*
     * Get the subsystem portion of the int equivalent of a routing token.
     * @return int The subsystem portion of the int equivalent of a
     * routing token.
     */

    private final static int getSubsystemId(int id) {
        return id & SUBSYSTEM_MASK;
    }

    
    /**
     * Get the unique id of this routing token.
     * @return int The unique id of this routing token within the subsystem.
     */

    public final int getUniqueId() {
        return (id >> SUBSYSTEM_SHIFT);
    }


    /**
     * Get the unique id of this routing token.
     * @param routingId The raw int equivalent of the routing token.
     * @return int The unique id of this routing token within the subsystem.
     */

    public static final int getUniqueId(int routingId) {
        return (routingId >> SUBSYSTEM_SHIFT);
    }


    /**
     * Get a routing token that is unique within the subsystem of the this
     * RoutingToken.
     * @return int A routing token that is unique the subsystem of the
     * this RoutingToken.
     */

    public synchronized RoutingToken getUniqueRoutingToken() {
        return new RoutingToken((nextTokenNumber++ << SUBSYSTEM_SHIFT) |
				getSubsystemId());
    }


    /**
     * Same as getUniqueRoutingToken, but returns integer (no need for
     * intermediate object)
     */

    public synchronized int getUniqueRoutingTokenInt() {
        return (nextTokenNumber++ << SUBSYSTEM_SHIFT) | getSubsystemId();
    }


    /**
     * Is the given routing token in the same subsystem as this object?
     * @param routingToken The RoutingToken to test.
     * @return boolean True iff routingToken is in the same subsystem as
     * this object.
     */

    public final boolean isSameSubsystem(RoutingToken routingToken) {
	return (routingToken.getSubsystemId() == getSubsystemId());
    }


    /**
     * Is the given routing token in the same subsystem as this object?
     * @param routingToken The raw int equivalent of the routing token to test.
     * @return boolean True iff routingToken is in the same subsystem as
     * this object.
     */

    public final boolean isSameSubsystem(int routingToken) {
	return (getSubsystemId(routingToken) == getSubsystemId(id));
    }

    
    /**
     * Is this object a target of the routing token?
     * return boolean True iff routingToken should target this object.
     * I.e., routingToken is the broadcast token in the same subsystem or
     * is the broadcast token in the BROADCAST_TOKEN subsystem or
     * routingToken is the same as this object.
     */

    public final boolean isTargetOf(int routingToken) {
        return ((routingToken == BROADCAST_TOKEN) ||
                (getUniqueId(routingToken) == BROADCAST_TOKEN) && isSameSubsystem(routingToken)) ||
                (id == routingToken);
    }

    
    public static final String toString(int id) {
	String uniqueId = (getUniqueId(id) == BROADCAST_TOKEN) ? "BROADCAST" : Integer.toString(getUniqueId(id)); // NOI18N

	String systemId = "";
	if (BREAKPOINTS.isSameSubsystem(id))
	    systemId = "BREAKPOINTS";   // NOI18N
	if (WATCHES.isSameSubsystem(id))
	    systemId = "WATCHES";   // NOI18N
	else if (DISPLAY_ITEM_MANAGER.isSameSubsystem(id))
	    systemId = "DIM";   // NOI18N
	else if (TREETABLE.isSameSubsystem(id))
	    systemId = "TREETABLE";     // NOI18N
	else if (WATCH.isSameSubsystem(id))
	    systemId = "WATCH"; // NOI18N
	else if (VAR.isSameSubsystem(id))
	    systemId = "VAR"; // NOI18N
	else
	    systemId = Integer.toString(getSubsystemId(id));
	return "RoutingToken[" + uniqueId + "/" + systemId + "]";       //NOI18N
    }

    
    @Override
    public final String toString() {
        return toString(id);
    }

    /**
     * The code must be constructed so that these objects end up with the
     * getUniqueId equivalent of BROADCAST_TOKEN. Also, just so we don't
     * recognize an uninitialized zero as some valid token, don't start
     * with zero.
     */

    public static final RoutingToken BREAKPOINTS = new RoutingToken(1);
    // DisplayItemManager:
    public static final RoutingToken DISPLAY_ITEM_MANAGER = new RoutingToken(2);
    // TreeTableModel:
    public static final RoutingToken TREETABLE = new RoutingToken(3);
    // WatchWindow:
    public static final RoutingToken WATCH = new RoutingToken(4);
    // VarContinuation:
    public static final RoutingToken VAR = new RoutingToken(5);
    public static final RoutingToken WATCHES = new RoutingToken(6);
}
