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

package org.netbeans.modules.schema2beans;



/**
 *  Trace flag values
 */
public class DDLogFlags extends Object
{
    public String dbgNames[];
    public Object actionSets[];

    //	DTD/Internal tree Parsing traces
    public static final int DBG_DTD		= 12;

    //	Internal Tree traces (BeanProp/DOMBinding...)
    public static final int DBG_BLD		= 13;

    //	Events traces
    public static final int DBG_EVT		= 14;

    //	User Bean traces (BaseBean)
    public static final int DBG_UBN		= 15;

    //	Registry traces (DDRegistry)
    public static final int DBG_REG		= 16;
    
    /**
     * Maximum group level trace - please maintain up to date.
     */
    public static final int MAX_DBG_GROUP 	= 16;
    
    
    /**
     * Can be dynamically changed to trigger the traces 
     * (see GraphManager.debug() method)
     */
    public static boolean debug = false;
    public static void setDebug(boolean d) {debug = d;}
    static {
        // Use -J-Dschema2beans.debug=true to runide
        String externalDebug = System.getProperty("schema2beans.debug");
        if (externalDebug != null) {
            debug = true;
        }
    }

    
    //
    // Action ids - DOM_BASIC tracing
    //
    public static final int FINDNODE	   	= 1;
    public static final int GETTEXT	 	= 2;
    public static final int SETTEXT	 	= 3;
    public static final int GETIDXTEXT 		= 4;
    public static final int SETIDXTEXT 		= 5;
    public static final int DDCREATE		= 6;
    public static final int DDCREATED		= 7;
    public static final int DDBEANED		= 8;
    public static final int FOUNDNODE		= 9;
    public static final int BOUNDNODE		= 10;
    public static final int NONODE		= 11;
    public static final int NOTELT		= 12;
    public static final int BINDPROP		= 13;
    public static final int EXCEEDED		= 14;
    public static final int SETVALUE		= 15;
    public static final int NEWBIND		= 16;
    public static final int SYNCNODES		= 17;
    public static final int CACHING		= 18;
    public static final int SYNCING		= 19;
    public static final int DELETENODE		= 20;
    public static final int BEANCOMP		= 21;
    public static final int PROPCOMP		= 22;
    public static final int CREATEATTR		= 23;
    public static final int GETATTR		= 24;
    
    //
    // Action names - Internal tree tracing
    //
    static final String[] bldActionNames =	// BEGIN_NOI18N
    {
	"FINDNODE   ",
	"GETTEXT    ",
	"SETTEXT    ",
	"GETIDXTEXT ",
	"SETIDXTEXT ",
	"DDCREATE   ",
	"DDCREATED  ",
	"DDBEANED   ",
	"FOUNDNODE  ",
	"BOUNDNODE  ",
	"NONODE     ",
	"NOTELT     ",
	"BINDPROP   ",
	"EXCEEDED   ",
	"SETVALUE   ",
	"NEWBIND    ",
	"SYNCNODES  ",
	"CACHING    ",
	"SYNCING    ",
	"DELETENODE ",
	"BEANCOMP   ",
	"PROPCOMP   ",
	"CREATEATTR ",
	"GETATTR "
    };					// END_NOI18N
    
    
    //
    // Action ids - DTD parsing tracing
    //
    public static final int STARTDOC	   	= 1;
    public static final int ENDDOC	 	= 2;
    public static final int STARTELT	   	= 3;
    public static final int ENDELT 		= 4;
    public static final int ELEMENT 		= 5;
    public static final int STARTGRP 		= 6;
    public static final int ENDGRP 		= 7;
    
    //
    // Action names - DOM_BASIC tracing
    //
    static final String[] dtdActionNames =	// BEGIN_NOI18N
    {
	"STARTDOC   ",
	"ENDDOC     ",
	"STARTELT   ",
	"ENDELT     ",
	"ELEMENT    ",
	"STARTGRP   ",
	"ENDGRP     "
    };					// END_NOI18N
    
    //
    // Events ids
    //    
    public static final int CREATECHG  	= 1;
    public static final int CREATEREM  	= 2;
    public static final int NOTIFYCHG  	= 3;
    public static final int NOTIFYREM  	= 4;
    public static final int VETOABLE  	= 5;
    public static final int NOTIFYVETO 	= 6;
    
    
    //
    // Action names - DOM_BASIC tracing
    //
    static final String[] evtActionNames =	// BEGIN_NOI18N
    {
	"CREATECHG  ",
	"CREATEREM  ",
	"NOTIFYCHG  ",
	"NOTIFYREM  ",
	"VETOABLE   ",
	"NOTIFYVETO "
    };					// END_NOI18N
    
    
    //
    // User bean traces
    //    
    public static final int MERGE		= 1;
    public static final int MERGEPROP		= 2;
    public static final int MERGEFOUND		= 3;
    public static final int MERGENTFND		= 4;
    public static final int EQUALS		= 5;
    public static final int FIND		= 6;
    public static final int FINDATTR		= 7;
    public static final int FINDCMP		= 8;
    public static final int FNDATTR		= 9;
    public static final int FINDPROP		= 10;
    public static final int FNDPROP		= 11;
    
    
    //
    // Action names - DOM_BASIC tracing
    //
    static final String[] ubnActionNames =	// BEGIN_NOI18N
    {
	"MERGE      ",
	"MERGEPROP  ",
	"MERGEFOUND ",
	"MERGENTFND ",
	"EQUALS     ",
	"FIND       ",
	"FINDATTR   ",
	"FINDCMP    ",
	"FNDATTR    ",
	"FINDPROP   ",
	"FNDPROP    "
	// "EVENTNEW   ",
    };					// END_NOI18N
    
    
    //
    // Registry traces
    //
    
    public static final int ADDTYPE		= 1;
    
    
    //
    // Action names - DD Registry/Parsing tracing
    //
    static final String[] regActionNames =	// BEGIN_NOI18N
    {
	"ADDTYPE    ",
	//"           ",
    };					// END_NOI18N
    
    
    
    public DDLogFlags()			// BEGIN_NOI18N
    {
	super();
	dbgNames = new String[TraceLogger.MAXGROUP];
	dbgNames[DBG_DTD	     - 1] = "dtd";
	dbgNames[DBG_BLD	     - 1] = "ibn";
	dbgNames[DBG_EVT	     - 1] = "evt";
	dbgNames[DBG_UBN	     - 1] = "ubn";
	dbgNames[DBG_REG	     - 1] = "reg";
	
	actionSets = new Object[TraceLogger.MAXGROUP];
	actionSets[DBG_DTD	     	- 1] = dtdActionNames;
	actionSets[DBG_BLD	     	- 1] = bldActionNames;
	actionSets[DBG_EVT	     	- 1] = evtActionNames;
	actionSets[DBG_UBN	     	- 1] = ubnActionNames;
	actionSets[DBG_REG	     	- 1] = regActionNames;
    }					// END_NOI18N
}
