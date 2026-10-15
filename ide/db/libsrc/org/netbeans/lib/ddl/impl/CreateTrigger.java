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

package org.netbeans.lib.ddl.impl;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import org.openide.util.NbBundle;
import org.netbeans.lib.ddl.CreateTriggerCommand;
import org.netbeans.lib.ddl.DDLException;

/**
* Interface of database action command. Instances should remember connection
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*/

public class CreateTrigger extends AbstractCommand implements CreateTriggerCommand {
    public static final int BEFORE = 1;
    public static final int AFTER = 2;

    /** Arguments */
    private Vector events;

    /** for each row */
    boolean eachrow;

    /** Condition */
    private String cond;

    /** Table */
    private String table;

    /** Timing */
    int timing;

    /** Body of the procedure */
    private String body;

    public static String getTimingName(int code)
    {
        switch (code) {
        case BEFORE: return "BEFORE"; // NOI18N
        case AFTER: return "AFTER"; // NOI18N
        }

        return null;
    }

    static final long serialVersionUID =-2217362040968396712L;
    public CreateTrigger()
    {
        events = new Vector();
    }

    public String getTableName()
    {
        return table;
    }

    public void setTableName(String tab)
    {
        table = tab;
    }

    public boolean getForEachRow()
    {
        return eachrow;
    }

    public void setForEachRow(boolean flag)
    {
        eachrow = flag;
    }


    /** Returns text of procedure */
    public String getText()
    {
        return body;
    }

    /** Sets name of table */
    public void setText(String text)
    {
        body = text;
    }

    public String getCondition()
    {
        return cond;
    }

    public void setCondition(String con)
    {
        cond = con;
    }

    public int getTiming()
    {
        return timing;
    }

    public void setTiming(int time)
    {
        timing = time;
    }

    /** Returns arguments */
    public Vector getEvents()
    {
        return events;
    }

    public TriggerEvent getEvent(int index)
    {
        return (TriggerEvent)events.get(index);
    }

    /** Sets argument array */
    public void setEvents(Vector argarr)
    {
        events = argarr;
    }

    public void setEvent(int index, TriggerEvent arg)
    {
        events.set(index, arg);
    }

    public TriggerEvent createTriggerEvent(int when, String columnname)
    throws DDLException
    {
        try {
            Map gprops = (Map)getSpecification().getProperties();
            Map props = (Map)getSpecification().getCommandProperties(Specification.CREATE_TRIGGER);
            Map bindmap = (Map)props.get("Binding"); // NOI18N
            String tname = (String)bindmap.get("EVENT"); // NOI18N
            if (tname != null) {
                Map typemap = (Map)gprops.get(tname);
                if (typemap == null) throw new InstantiationException(
                    MessageFormat.format(
                        NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateObject"), // NOI18N
                        new String[] {tname}));
                Class typeclass = Class.forName((String)typemap.get("Class")); // NOI18N
                String format = (String)typemap.get("Format"); // NOI18N
                TriggerEvent evt = (TriggerEvent)typeclass.getDeclaredConstructor().newInstance();
                Map temap = (Map)props.get("TriggerEventMap"); // NOI18N
                evt.setName(TriggerEvent.getName(when));
                evt.setColumn(columnname);
                evt.setFormat(format);
                return (TriggerEvent)evt;
            } else throw new InstantiationException(
                    MessageFormat.format(
                        NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateType"), // NOI18N
                        new String[] {"EVENT", bindmap.toString() })); // NOI18N
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }

    public void addTriggerEvent(int when)
    throws DDLException
    {
        addTriggerEvent(when, null);
    }

    public void addTriggerEvent(int when, String columnname)
    throws DDLException
    {
        TriggerEvent te = createTriggerEvent(when, columnname);
        if (te != null) events.add(te);
    }

    public Map getCommandProperties() throws DDLException {
        Map props = (Map)getSpecification().getProperties();
        String evs = "", argdelim = (String)props.get("TriggerEventListDelimiter"); // NOI18N
        Map cmdprops = super.getCommandProperties();

        Enumeration col_e = events.elements();
        while (col_e.hasMoreElements()) {
            TriggerEvent evt = (TriggerEvent)col_e.nextElement();
            boolean inscomma = col_e.hasMoreElements();
            evs = evs + evt.getCommand(this)+(inscomma ? argdelim : "");
        }

        cmdprops.put("trigger.events", evs); // NOI18N
        cmdprops.put("trigger.condition", cond); // NOI18N
        cmdprops.put("trigger.timing", getTimingName(timing)); // NOI18N
        cmdprops.put("table.name", quote(table)); // NOI18N
        cmdprops.put("trigger.body", body); // NOI18N
        if (eachrow)
            cmdprops.put("each.row", ""); // NOI18N
        
        return cmdprops;
    }
}
