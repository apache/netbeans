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
package org.netbeans.modules.versioning.ui.history;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 *
 */
class RevisionNode extends AbstractNode implements Comparable {
    
    static final String PROPERTY_NAME_LABEL = "label";                          // NOI18N        
    static final String PROPERTY_NAME_USER = "user";                            // NOI18N        
    static final String PROPERTY_NAME_VERSION = "version";                      // NOI18N                       

    private HistoryEntry entry; 
    private static DateFormat dateFormat = DateFormat.getDateTimeInstance();                      
    private static DateFormat timeFormat = DateFormat.getTimeInstance();

    private RevisionNode(HistoryEntry entry, Lookup l) {                
        super(createChildren(entry), l);                        
        this.entry = entry;
        initProperties();
    }        
         
    static RevisionNode create(HistoryEntry entry) {
        List<Object> lookup = new LinkedList<Object>();
        VCSFileProxy[] proxies = entry.getFiles();
        for (VCSFileProxy proxy : proxies) {
            lookup.add(proxy);
            File f = proxy.toFile();
            if(f != null) {
                lookup.add(f);
            }
        }
        lookup.addAll(Arrays.asList(entry.getLookupObjects()));
        lookup.add(entry);
        return new RevisionNode(entry, Lookups.fixed(lookup.toArray(new Object[0])));
    }
    
    private static Children createChildren(HistoryEntry entry) {
        if(entry.getFiles().length == 1) {
            return Children.LEAF;
        } else {
            FileNode[] nodes = new FileNode[entry.getFiles().length];
            int i = 0;
            for (VCSFileProxy file : entry.getFiles()) {
                nodes[i++] = new FileNode(entry, file);            
            }
            Children.SortedArray children = new Children.SortedArray();            
            children.add(nodes);
            return children;        
        }
    }
        
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
                
        ps.put(new RevisionProperty()); // XXX show only if VCS available
        ps.put(new UserProperty()); 
        ps.put(entry.canEdit() ? new EditableMessageProperty() : new MessageProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }   
    
    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getName() {                
        return getFormatedDate(entry);
    }    

    @Override
    public String toString() {
        return getName();
    }
    
    static String getFormatedDate(HistoryEntry se)  {
        int day = getDay(se.getDateTime().getTime());
        switch(day) {
            case 0:  return NbBundle.getMessage(RevisionNode.class, "LBL_Today", new Object[] {timeFormat.format(se.getDateTime())});   
            case 1:  return NbBundle.getMessage(RevisionNode.class, "LBL_Yesterday", new Object[] {timeFormat.format(se.getDateTime())});
            default: return dateFormat.format(se.getDateTime());
        }
    }
    
    private static int getDay(long ts) {
        Date date = new Date(ts);
                
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());        
        
        // set the cal at today midnight
        int todayMillis = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 +
                          c.get(Calendar.MINUTE)      * 60 * 1000 + 
                          c.get(Calendar.SECOND)      * 1000 + 
                          c.get(Calendar.MILLISECOND);                
        c.add(Calendar.MILLISECOND, -1 * todayMillis);                        
        
        if(c.getTime().compareTo(date) < 0) {
            return 0;
        }
        
        return (int) ( (c.getTimeInMillis() - ts) / (24 * 60 * 60 * 1000) ) + 1;
                
    }    
    @Override
    public Action[] getActions(boolean context) {
        return entry.getActions();          
    }

    @Override
    public int compareTo(Object obj) {
        if(!(obj instanceof RevisionNode)) {
            return -1;
        }
        RevisionNode node = (RevisionNode) obj;
        return entry.getDateTime().compareTo(node.entry.getDateTime());
    }
    
    class MessageProperty extends PropertySupport.ReadOnly<TableEntry> {
        private TableEntry te;
        public MessageProperty() {
            super(PROPERTY_NAME_LABEL, TableEntry.class, NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Desc"));
            te = new TableEntry() {
                @Override
                public String getDisplayValue() {
                    return entry.getMessage();
                }
                @Override
                public String getTooltip() {
                    return entry.getMessage();
                }
            };   
        }
        @Override
        public TableEntry getValue() throws IllegalAccessException, InvocationTargetException {
            return te;
        }

        @Override
        public String toString() {
            return entry.getMessage();
        }
    }
    
    class EditableMessageProperty extends PropertySupport.ReadWrite<TableEntry> {
        private TableEntry te;
        public EditableMessageProperty() {
            super(PROPERTY_NAME_LABEL, TableEntry.class, NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Desc"));
            te = new MsgEntry(entry);   
        }
        @Override
        public TableEntry getValue() throws IllegalAccessException, InvocationTargetException {
            return te;
        }    
        @Override        
        public void setValue(TableEntry te) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
        {        
            this.te = te;
        }        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                @Override
                public void setAsText(String text) throws IllegalArgumentException {
                    if (text instanceof String) {
                        try {
                            entry.setMessage(!text.equals("") ? text : null);
                        } catch (IOException ex) {
                            History.LOG.log(Level.WARNING, null, ex);
                        }
                        return;
                    }
                    throw new java.lang.IllegalArgumentException(text);
                }
                @Override
                public String getAsText() {
                    return te.getDisplayValue();
                }
            };
        }           
        
        @Override
        public String toString() {
            return entry.getMessage();
        }

        private class MsgEntry extends TableEntry {
            private final HistoryEntry entry;
            public MsgEntry(HistoryEntry entry) {
                this.entry = entry;
            }
            @Override
            public String getDisplayValue() {
                return entry.getMessage();
            }
            @Override
            public String getTooltip() {
                String tooltip = entry.getMessage();
                if(tooltip == null || "".equals(tooltip.trim())) {                       // NOI18N
                    tooltip = NbBundle.getMessage(RevisionNode.class, "LBL_SetTooltip"); // NOI18N
                }
                return tooltip;
            }

            @Override
            public String toString() {
                return entry.getMessage();
            }
            
            @Override
            public int hashCode() {
                int hash = 7;
                hash = 11 * hash + (this.entry.getMessage() != null ? this.entry.getMessage().hashCode() : 0);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final MsgEntry other = (MsgEntry) obj;
                String msg1 = entry.getMessage();
                String msg2 = other.entry.getMessage();
                if (msg1 == null || !msg1.equals(msg2)) {
                    return false;
                }
                return true;
            }
        }
    }                      
    
    class UserProperty extends PropertySupport.ReadOnly<TableEntry> {
        private TableEntry te;
        public UserProperty() {
            super(PROPERTY_NAME_USER, TableEntry.class, NbBundle.getMessage(RevisionNode.class, "LBL_UserProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_UserProperty_Desc"));
            te = new UserEntry(entry);            
        }
        @Override
        public TableEntry getValue() throws IllegalAccessException, InvocationTargetException {
            return te;
        }

        @Override
        public String toString() {
            return entry.getUsername();
        }

        private class UserEntry extends TableEntry {
            private final HistoryEntry entry;
            public UserEntry(HistoryEntry entry) {
                this.entry = entry;
            }
            @Override
            public String getDisplayValue() {
                return entry.getUsernameShort();
            }
            @Override
            public String getTooltip() {
                return entry.getUsername();
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 47 * hash + (this.entry.getUsernameShort() != null ? this.entry.getUsernameShort().hashCode() : 0);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final UserEntry other = (UserEntry) obj;
                String un1 = this.entry.getUsernameShort();
                String un2 = other.entry.getUsernameShort();
                if (un1 == null || !un1.equals(un2)) {
                    return false;
                }
                return true;
            }
        }
    }        
    
    class RevisionProperty extends PropertySupport.ReadOnly<TableEntry> {
        private final TableEntry te;
        public RevisionProperty() {
            super(PROPERTY_NAME_VERSION, TableEntry.class, NbBundle.getMessage(RevisionNode.class, "LBL_VersionProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_VersionProperty_Desc"));
            te = new RevisionEntry(entry);
        }
        @Override
        public TableEntry getValue() throws IllegalAccessException, InvocationTargetException {
            return te;
        }

        @Override
        public String toString() {
            return entry.getRevision();
        }

        private class RevisionEntry extends TableEntry {
            private final HistoryEntry entry;

            private RevisionEntry(HistoryEntry entry) {
                this.entry = entry;
            }
            
            @Override
            public String getDisplayValue() {
                return entry.getRevisionShort();
            }

            @Override
            public String getTooltip() {
                return entry.getRevision();
            }

            @Override
            public int compareTo(TableEntry e) {
                if(e == null) return 1;
                Integer i1;
                Integer i2;
                try {
                    i1 = Integer.parseInt(getDisplayValue());
                    i2 = Integer.parseInt(e.getDisplayValue());
                    return i1.compareTo(i2);
                } catch (NumberFormatException ex) {}
                return super.compareTo(e);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                String r1 = this.entry.getRevisionShort();
                String r2 = ((RevisionEntry)obj).entry.getRevisionShort();
                if (r1 == null || !r1.equals(r2)) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 71 * hash + (this.entry.getRevisionShort() != null ? this.entry.getRevisionShort().hashCode() : 0);
                return hash;
            }
        }
    } 

    static class FileNode extends AbstractNode implements Comparable {        

        private final HistoryEntry entry;
        private final VCSFileProxy file;
        
        FileNode(HistoryEntry entry, VCSFileProxy file) {
            super(Children.LEAF, createLookup(file, entry)); 
            this.entry = entry;
            this.file = file;
        }
    
        private static Lookup createLookup(VCSFileProxy proxy, HistoryEntry entry) {
            List<Object> lookup = new LinkedList<Object>();
            lookup.add(proxy);
            File f = proxy.toFile();
            if(f != null) {
                lookup.add(f);
            }
            lookup.add(entry);
            lookup.addAll(Arrays.asList(entry.getLookupObjects()));
            return Lookups.fixed(lookup.toArray(new Object[0]));
        }        
    
        @Override
        public Action[] getActions(boolean context) {
            return entry.getActions();       
        }

        @Override
        public String getName() {
            return file.getName(); 
        }  
        
        @Override
        public int compareTo(Object obj) {
            if(!(obj instanceof FileNode)) {
                return -1;
            }
            FileNode node = (FileNode) obj;        
            return getName().compareTo(node.getName());            
        }        
    }    
}

