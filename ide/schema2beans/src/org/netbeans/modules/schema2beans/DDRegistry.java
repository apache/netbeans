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

import java.util.*;

import java.beans.*;

/**
 *  The intent of this class is to provide a schema2beans graph registry,
 *  where the graphs are stored by unique name and type. Combined with
 *  the DDRegistryParser, the DDRegistry provides a sort of meta-schema2beans
 *  graph where we can ask for all the graphs of one type and ask for a
 *  parser on any set of graphs. These are the two main goals of this class.
 *
 *    1. provide a common and central place where we can keep track
 *       of all the graphs that we have created. Naming the graphs by 
 *       unique name and type allows to get either one specific graph 
 *       or a set of graphs.
 *
 *    2. provide a meta-schema2beans graph view for parsing any set of graphs. 
 *       For example, to get a parser on all the graphs of type 'ejb' 
 *       (assuming we registered graphs of this type).
 *
 *  Therefore, this class provides two kind of methods: to add/remove graphs
 *  to the registry and to create a DDRegistry parser. A DDRegistryParser is
 *  an Iterator that returns all the elements described a schema2beans tree path. 
 *
 *  The registry also provides a convenient class (DDChangeMarker) that helps
 *  keeping track of any change.
 */
public class DDRegistry extends Object {
    
    /**
     *	The goal of this class is to provide a simple way to know if a graph
     *	or a set of graphs had their content changed. The ChangeMarker can
     *	be used to implemenent a cache mechanism, in order to avoid parsing
     *	the graphs if nothing in the graph changed since the last parsing.
     *
     *	A ChangeMarker is created by the registry, ddReg.newChangeMarker().
     *	We can add to the changeMarger a BaseBean graph, a Cursor or another
     *	ChangeMarker. So, ChangeMarker can be nested, and any modification in
     *	a nested ChangeMarker would need that the upper ChangeMarker have been
     *  been modified.
     */
    public static class DDChangeMarker {
	
	private DDRegistry reg;
	private long timestamp;
	
	private ArrayList elts = null;
	
	DDChangeMarker(DDRegistry reg) {
	    this.reg = reg;
	    this.elts = new ArrayList();
	    this.timestamp = 0L;
	}
	
	
	public int size() {
	    return this.elts.size();
	}
	
	/**
	 *  If any graph of the added marker change, the current marker is
	 *  also considered changed.
	 */
	public void add(DDChangeMarker cm) {
	    if (cm == this) {
		Thread.dumpStack();
	    }
	    this.elts.add(cm);
	}
	
	/**
	 *  Add a graph to the marker list. If any change occurs in this graph
	 *  after resetMarker() is called, hasChanged() would return true.
	 */
	public void add(BaseBean b) {
	    RegEntry re = this.reg.getRegEntry(b, false);
	    if (re != null && !this.elts.contains(re)) {
		this.elts.add(re);
	    }
	}
	
	/**
	 *  Add the graph the cursor belongs to, to the marker list
	 */
	public void add(DDRegistryParser.DDCursor c) {
	    String id = this.reg.getID(c);
	    if (id != null) {
		BaseBean b = this.reg.getRoot(id);
		this.add(b);
	    }
	}
	
	/**
	 *  removal methods.
	 */
	public void remove(DDChangeMarker cm) {
	    this.elts.remove(cm);
	}
	
	public void remove(BaseBean b) {
	    RegEntry re = this.reg.getRegEntry(b, false);
	    if (re != null)
		this.elts.remove(re);
	}
	
	public void remove(DDRegistryParser.DDCursor c) {
	    String id = this.reg.getID(c);
	    if (id != null) {
		BaseBean b = this.reg.getRoot(id);
		this.remove(b);
	    }
	}

	/**
	 *  Reset the marke change time. Any change that happened before now
	 *  are ignored.
	 */
	public void resetTime() {
	    this.timestamp = System.currentTimeMillis();
	}
	
	/**
	 *  Return true if a change event happen between the last resetTime
	 *  and now.
	 */
	public boolean hasChanged() {
	    boolean b = this.hasChanged(this.timestamp);
	    return b;
	}
	
	private boolean hasChanged(long ts) {
	    for(int i=0; i<this.elts.size(); i++) {
		Object o = this.elts.get(i);
		
		if (o == null)
		    continue;
		
		if (o instanceof DDChangeMarker) {
		    if (((DDChangeMarker)o).hasChanged(ts)) {
			return true;
		    }
		} else
		    if (o instanceof RegEntry) {
			if (((RegEntry)o).getTimestamp() > ts) {
			    return true;
			}
		    }
	    }
	    return false;
	}

	/**
	 *  Dump all the registered markers
	 */
	public String dump() {
	    return this.dump(new StringBuffer(), "",	// NOI18N
			     this.timestamp).toString();
	}
	
	public StringBuffer dump(StringBuffer sb, String indent, long ts) {	// BEGIN_NOI18N
	    
	    sb.append(indent + this.toString() + "\n");
	    
	    for(int i=0; i<this.elts.size(); i++) {
		Object o = this.elts.get(i);
		
		if (o == null)
		    continue;
		
		if (o instanceof DDChangeMarker) {
		    ((DDChangeMarker)o).dump(sb, indent + "  ", ts);
		} else
		    if (o instanceof RegEntry) {
			RegEntry re = (RegEntry)o;
			sb.append(indent + "  " + re.getBean() + "-0x" +
			Integer.toHexString(re.getBean().hashCode()));
			long l = re.getTimestamp();
			if (l > ts) {
			    sb.append(" Changed  (bean ts:" + l 
				      + " > cm ts:" + ts );
			    //    + ") - last event: " + re.getLastEvent());
			} else {
			    sb.append(" No_Change (bean ts:" + l 
				      + " < cm ts:" + ts + ")");
			}
			sb.append("\n");
		    }
	    }
	    return sb;
	}									// END_NOI18N
	
	public String toString() {
	    return "DDChangeMarker-0x" + Integer.toHexString(this.hashCode());	// NOI18N
	}
    }
    
    /*
     *	Change event listener used by the change marker class
     */
    public class ChangeTracer implements PropertyChangeListener {
	DDRegistry 	reg;
	
	public ChangeTracer(DDRegistry reg) {
	    this.reg = reg;
	}
	
	public void propertyChange(PropertyChangeEvent e) {
	    
	    try {
		BaseBean s = (BaseBean)e.getSource();
		RegEntry re = this.reg.getRegEntry(s, false);
		re.setTimestamp();
		//String trc = 
		//    s.graphManager().getKeyPropertyName(e.getPropertyName());
		//re.setLastEvent(trc);
	    } catch(Exception ex) {
	    }
	}
    }
    
    private ArrayList		scopes;
    private ChangeTracer 	changeTracer;


    //
    public DDRegistry() {
	this.scopes = new ArrayList();
	this.changeTracer = new ChangeTracer(this);
    }
    
    /**
     *  Create a new entry in the DD graph registry. The schema2beans graph
     *  bean is added to registry using a unique name (ID), such as a unique
     *  internal identifier, and a non unique name (name), 
     *  such as a display name.
     *  
     *  Any number of non unique type can also be associated to a graph
     *  entry, see the method addType.
     *
     */
    public void createEntry(BaseBean bean, String ID, String name) {
	RegEntry entry = this.getRegEntry(bean, false);
	if (entry != null) {
	    throw new IllegalArgumentException(Common.getMessage(
	    "BeanGraphAlreadyInRegistry_msg", bean.name()));
	}
	
	entry = this.getRegEntry(ID);
	if (entry != null) {
	    throw new IllegalArgumentException(Common.getMessage(
	    "CantRegisterGraphSameID_msg", bean.name(), entry, ID));
	}
	bean.addPropertyChangeListener(this.changeTracer);
	this.scopes.add(new RegEntry(bean, ID, name));
    }

    /**
     *	Change the schema2beans graph for the unique entry ID. This method
     *  might be used if another graph should replace an existing entry.
     */
    public void updateEntry(String ID, BaseBean bean) {
	RegEntry entry = this.getRegEntry(ID);
	if (entry != null)
	    entry.setBean(bean);
	else
	    throw new IllegalArgumentException(Common.getMessage(
	    "CantUpdateGraphNotInRegistry_msg", ID));
    }
    
    /**
     *	Remove an entry in the registry.
     */
    public void removeEntry(BaseBean bean) {
	RegEntry entry = this.getRegEntry(bean, false);
	if (entry != null) {
	    entry.getBean().removePropertyChangeListener(this.changeTracer);
	    this.removeRegEntry(entry);
	}
    }
    
    /**
     *  Rename a graph entry unique ID to a new unique ID entry and new
     *  non unique name.
     */
    public void renameEntry(String oldID, String newID, String newName) {
	RegEntry entry = this.getRegEntry(oldID);
	if (entry != null) {
	    entry.setID(newID);
	    if (newName != null)
		entry.setName(newName);
	}
    }

    /**
     *  Rename a graph unique ID to a new unique ID.
     */
    public void renameEntry(String oldID, String newID) {
	this.renameEntry(oldID, newID, null);
    }

    /**
     *  Remove a registry entry.
     */
    public void removeEntry(String ID) {
	RegEntry entry = this.getRegEntry(ID);
	if (entry != null)
	    entry.getBean().removePropertyChangeListener(this.changeTracer);
	this.removeRegEntry(entry);
    }
    
    /**
     *  This return a new change marker instance, that can be used to know
     *  if any graph of the registry has changed.
     */	
    public DDChangeMarker createChangeMarker() {
	return new DDChangeMarker(this);
    }
    
    /**
     *	Return true of the specified schema2beans graph is registered with the
     *  specified type.
     */
    public boolean hasType(BaseBean bean, String type) {
	RegEntry r = this.getRegEntry(bean, false);
	if (r != null)
	    return r.hasType(type);
	return false;
    }
    
    /**
     *  Reset the change timestamp of all the registered graphs.
     */
    public void clearCache() {
	for (int i=0; i<this.scopes.size(); i++) {
	    RegEntry se = (RegEntry)this.scopes.get(i);
	    if (se != null)
		se.setTimestamp();
	}
    }
    
    /**
     *  Reset the change timestamp of the specified graph.
     */
    public void clearCache(BaseBean bean) {
	this.setTimestamp(bean);
    }
    
    public void setTimestamp(BaseBean bean) {
	RegEntry r = this.getRegEntry(bean, false);
	if (r != null)
	    r.setTimestamp();
    }
    
    public long getTimestamp(BaseBean bean) {
	RegEntry r = this.getRegEntry(bean, false);
	if (r != null)
	    return r.getTimestamp();
	return 0L;
    }

    /**
     *  Having a schema2beans graph, this method return its unique ID.
     */
    public String getID(BaseBean bean) {
	RegEntry r = this.getRegEntry(bean, false);
	if (r != null)
	    return r.getID();
	return null;
    }
    
    /**
     *  Return the unique ID of the graph where the cursor points to.
     *	Note that a DDCursor is a location reference (pointer to any 
     *  schema2beans graph) in any graph of the registry.
     */
    public String getID(DDRegistryParser.DDCursor c) {
	RegEntry r = this.getRegEntry(c.getRoot(), false);
	if (r != null)
	    return r.getID();
	return null;
    }
    
    /**
     *	Same as getID but return the non unique name.
     */
    public String getName(DDRegistryParser.DDCursor c) {
	RegEntry r = this.getRegEntry(c.getRoot(), false);
	if (r != null)
	    return r.getName();
	return null;
    }
    

    /**
     *	Create a new DDRegistryParser (parser on a set of graph), based
     *  on another parser current location and schema2beans path.
     */
    public DDRegistryParser newParser(DDRegistryParser parser, String path) {
	return new DDRegistryParser(this, parser, path);
    }
    
    /**
     *  Create a new DDRegistryParser based on a current position in a graph
     *  and a schema2beans path.
     */
    public DDRegistryParser newParser(DDRegistryParser.DDCursor cursor,
				      String path) {
	return new DDRegistryParser(this, cursor, path);
    }
    
    /**
     *  Create a DDRegistryParser based on a scope value. A scope value is
     *  either a unique graph name or a type value. If more than one graph
     *  are registered under the type specified, the parser will go through
     *  all the graphs. If the ID is specified or the scope is a one graph type,
     *  the the parser will go through only the specified graph.
     *  
     *  The scope syntax is: [name]
     */
    public DDRegistryParser newParser(String scope) {
	return new DDRegistryParser(this, scope);
    }
    
    /**
     *	Create a cursor location
     */
    public DDRegistryParser.DDCursor newCursor(DDRegistryParser.DDCursor c,
					       String path) {
	return new DDRegistryParser.DDCursor(c, path);
    }
    
    /**
     *	Create a cursor location
     */
    public DDRegistryParser.DDCursor newCursor(String path) {
	return new DDRegistryParser.DDCursor(this, path);
    }
    
    /**
     *	Create a cursor location
     */
    public DDRegistryParser.DDCursor newCursor(BaseBean bean) {
	return new DDRegistryParser.DDCursor(this, bean);
    }
    
    /**
     *	Add a type to the entry (the type doesn't have to be unique)
     */
    public void addType(BaseBean bean, String type) {
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_REG, 1, DDLogFlags.ADDTYPE,
	    bean.name() + " " +  type);
	}
	
	RegEntry se = this.getRegEntry(bean, true);
	se.add(type);
    }

    /**
     *  Return the name of the unique graph ID
     */
    public String getName(String ID) {
	RegEntry r = this.getRegEntry(ID);
	if (r!= null)
	    return r.getName();
	return null;
    }
    
    /**
     *  Trace/debug method. Return the list of all the registered graphs
     */
    public String dump() {
	StringBuffer s = new StringBuffer();
	for (int i=0; i<this.scopes.size(); i++) {
	    RegEntry se = (RegEntry)this.scopes.get(i);
	    if (se != null) {
		s.append(se.toString() + "\n");	// NOI18N
	    }
	}
	return s.toString();
    }
    
    /**
     *  Trace/debug method. Return the XML content of all the registered graphs.
     */
    public String dumpAll() {
	StringBuffer s = new StringBuffer();
	for (int i=0; i<this.scopes.size(); i++) {
	    RegEntry se = (RegEntry)this.scopes.get(i);
	    if (se != null) {
		s.append("\n*** Graph:" + se.toString() + "\n");	// NOI18N
		s.append(se.getBean().dumpBeanNode());
	    }
	}
	return s.toString();
    }
    
    /**
     *	Return the entry for the BaseBean, if any.
     */
    RegEntry getRegEntry(BaseBean bean, boolean raise) {
	for (int i=0; i<this.scopes.size(); i++) {
	    RegEntry se = (RegEntry)this.scopes.get(i);
	    if (se.getBean() == bean) {
		return se;
	    }
	}
	//	Didn't find it - try to look for the root
	if ((bean != null) && !bean.isRoot()) {
	    do {
		bean = bean.parent();
	    } while(bean != null && !bean.isRoot());
	    return this.getRegEntry(bean, raise);
	}
	
	if (raise) {
	    throw new IllegalArgumentException(Common.getMessage(
	    "BeanGraphEntryNotInRegistry_msg", bean.name()));
	} else
	    return null;
    }
    
    /**
     *	Return the entry for the BaseBean, if any.
     */
    private RegEntry getRegEntry(String ID) {
	for (int i=0; i<this.scopes.size(); i++) {
	    RegEntry se = (RegEntry)this.scopes.get(i);
	    if (se.getID().equals(ID))
		return se;
	}
	return null;
    }
    
    /**
     *	Return the entry for the BaseBean, if any.
     */
    private void removeRegEntry(RegEntry entry) {
	int i = this.scopes.indexOf(entry);
	if (i != -1)
	    this.scopes.remove(i);
    }
    
    //	Remove any blank and [] characters
    public static String getGraphName(String s) {
	if (s != null) {
	    s = s.trim();
	    if (s.startsWith("[")) {	// NOI18N
		int i = s.indexOf(']');
		if (i != -1)
		    s = s.substring(1,i);
		else
		    return null;
	    }
	}
	return s;
    }

    /*
     *  Check if there is any graph of this name in the registry
     */
    public boolean hasGraph(String str) {
	return this.hasGraph(str, null);
    }
    
    public boolean hasGraph(String str, DDRegistryParser.DDCursor c) {
	String s = getGraphName(str);
	if (s != null) {
	    if (!s.equals(DDRegistryParser.CURRENT_CURSOR))
		return (this.getRoot(s) != null);
	    else {
		if (c == null)
		    return true;
		else
		    return (this.getID(c) != null);
	    }
	}
	return false;
    }
    
    public static String createGraphName(String s) {
	if (s != null) {
	    //s = s.trim();
	    int i = s.indexOf('[');
	    if (i != -1 && i<s.indexOf(']'))
		return s;
	    else
		return "["+s+"]";	// NOI18N
	} else {
	    throw new IllegalArgumentException(Common.getMessage(
	    "GraphNameCantBeNull_msg"));
	}
    }
    
    /**
     *	Return the bean root for this name (either unique name or scope)
     */
    public BaseBean getRoot(String s) {
	BaseBean[] r = this.getRoots(s);
	if (r.length>0)
	    return r[0];
	return null;
    }
    
    /**
     *	Return all the bean roots for this name (either unique name or scope)
     */
    public BaseBean[] getRoots(String s) {
	s = getGraphName(s);
	//	Try to get the root by name, then by type
	RegEntry se = this.getRegEntry(s);
	if (se == null) {
	    ArrayList list = new ArrayList();
	    for (int i=0; i<this.scopes.size(); i++) {
		se = (RegEntry)this.scopes.get(i);
		if (se.hasType(s))
		    list.add(se.getBean());
	    }
	    BaseBean[] ret = new BaseBean[list.size()];
	    return (BaseBean[])list.toArray(ret);
	} else
	    return (new BaseBean[] {se.getBean()});
    }
    
    /**
     *	Iterator on a list of DDParser
     */
    class IterateParser implements Iterator {
	private ArrayList 	list;
	private int			pos;
	public IterateParser() {
	    this.list = new ArrayList();
	}
	
	void addParser(BaseBean b, String parsingPath) {
	    this.list.add(new DDParser(b, parsingPath));
	}
	
	public boolean hasNext() {
	    if (pos < list.size())
		return true;
	    else
		return false;
	}
	
	public Object next() {
	    return this.list.get(pos++);
	}
	
	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }
    
    
    /**
     *	All the types a schema2beans graph belongs to.
     */
    
    static class RegEntry {
	private BaseBean 	bean;
	private ArrayList 	types;
	private String		ID;
	private String		name;
	private long		timestamp;
	//private String		info;
	
	RegEntry(BaseBean b, String ID, String name) {
	    this.bean = b;
	    this.types = new ArrayList();
	    this.ID = ID;
	    this.name = name;
	    this.setTimestamp();
	}
	
	/*  For trace purpose
	void setLastEvent(String e) {
	    this.info = e;
	}
	
	String getLastEvent() {
	    return this.info;
	    }*/
	
	//  For the caching mechanism (DDChangeMarker)
	void setTimestamp() {
	    this.timestamp = System.currentTimeMillis();
	}
	
	long getTimestamp() {
	    return this.timestamp;
	}
	
	void setBean(BaseBean bean) {
	    this.bean = bean;
	}
	
	BaseBean getBean() {
	    return this.bean;
	}
	
	String getName() {
	    return this.name;
	}
	
	String getID() {
	    return this.ID;
	}
	
	void setID(String ID) {
	    this.ID = ID;
	}
	
	void setName(String name) {
	    this.name = name;
	}
	
	void add(String type) {
	    this.types.add(type);
	}
	
	boolean hasType(String type) {
	    for (int i=0; i<this.types.size(); i++) {
		String t = (String)this.types.get(i);
		if (type != null && t.equals(type))
		    return true;
	    }
	    return false;
	}
	
	public String toString() {
	    String s = this.bean.name() + "(id:" + ID + "-name:" + name + ")" + " [";	// NOI18N
	    for (int i=0; i<this.types.size(); i++) {
		String t = (String)this.types.get(i);
		s += " " + t;	// NOI18N
	    }
	    return s + "]";		// NOI18N
	    
	}
    }
}
