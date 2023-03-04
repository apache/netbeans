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
