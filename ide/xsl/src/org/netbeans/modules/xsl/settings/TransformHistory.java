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
package org.netbeans.modules.xsl.settings;

import java.util.*;
import java.io.*;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.xsl.utils.TransformUtil;


/**
 * Transformation history of one XML or XSLT document. Used as FileObject attribute and also as TransformPanel model.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TransformHistory implements Serializable {
    /** Serial Version UID */
    private static final long serialVersionUID = -6268945703343989727L;

    /** Last selected XMLs with associated Outputs. Can be null. */
    private ListMap xmlOutputMap; // Map<String,String>

    /** Last selected XSLs with associated Outputs. Can be null. */
    private ListMap xslOutputMap; // Map<String,String>

    /** Automatically overwrite output. */
    private boolean overwriteOutput;

    /** What to do with output file: DO_NOTHING | APPLY_DEFAULT_ACTION | OPEN_IN_BROWSER. */
    private int processOutput;

    /** Do nothing with output file. */
    public static final int DO_NOTHING = 0;
    /** Apply default action on output file. */
    public static final int APPLY_DEFAULT_ACTION = 1;
    /** Open output file int browser. */
    public static final int OPEN_IN_BROWSER = 2;

    /** FileObject's attribute name. */
    public static final String TRANSFORM_HISTORY_ATTRIBUTE =
        "org.netbeans.modules.xsl.settings.TransformHistory"; // NOI18N

    //
    // init
    //

    /** Creates new TransformHistory.
     */
    public TransformHistory () {
        xmlOutputMap = null;
        xslOutputMap = null;
        overwriteOutput = false;
        processOutput = OPEN_IN_BROWSER;
    }


    public String[] getXMLs () {
        return getXMLOutputMap().getInputs();
    }

    public String getLastXML () {
        return getXMLOutputMap().getLastInput();
    }

    public String[] getXSLs () {
        return getXSLOutputMap().getInputs();
    }

    public String getLastXSL () {
        return getXSLOutputMap().getLastInput();
    }

    public String getXMLOutput (String xml) {
        return getXMLOutputMap().getOutput (xml);
    }

    public String getLastXMLOutput () {
        return getXMLOutput (getLastXML());
    }

    public String getXSLOutput (String xsl) {
        return getXSLOutputMap().getOutput (xsl);
    }

    public String getLastXSLOutput () {
        return getXSLOutput (getLastXSL());
    }

    public void addXML (String xml, String output) {
        getXMLOutputMap().put (xml, output);
    }

    public void addXSL (String xsl, String output) {
        getXSLOutputMap().put (xsl, output);        
    }

    public boolean isOverwriteOutput () {
        return overwriteOutput;
    }

    public void setOverwriteOutput (boolean overwrite) {
        overwriteOutput = overwrite;
    }

    public int getProcessOutput () {
        return processOutput;
    }

    public void setProcessOutput (int process) {
        processOutput = process;
    }


    private ListMap getXMLOutputMap () {
        if ( xmlOutputMap == null ) {
            xmlOutputMap = new ListMap();
        }
        return xmlOutputMap;
    }

    private ListMap getXSLOutputMap () {
        if ( xslOutputMap == null ) {
            xslOutputMap = new ListMap();
        }
        return xslOutputMap;
    }

    public String toString () {
        StringBuffer sb = new StringBuffer (super.toString());
        sb.append (" [ xmlOutputMap= ").append (xmlOutputMap);
        sb.append (", xslOutputMap= ").append (xslOutputMap);
        sb.append (", overwriteOutput= ").append (overwriteOutput);
        sb.append (", processOutput= ").append (processOutput).append (" ]");
        return sb.toString();
    }
    
    public boolean equals (Object obj) {
        if ( ( obj instanceof TransformHistory ) == false ) {
            return false;
        }
        TransformHistory peer = (TransformHistory)obj;
        if ( equals (this.xmlOutputMap, peer.xmlOutputMap) == false ) {
            return false;
        }
        if ( equals (this.xslOutputMap, peer.xslOutputMap) == false ) {
            return false;
        }
        if ( this.overwriteOutput != peer.overwriteOutput ) {
            return false;
        }
        if ( this.processOutput != peer.processOutput ) {
            return false;
        }
        return true;
    }
    
    
    //
    // utils
    //
    static boolean equals (Object obj1, Object obj2) {
        if ( obj1 != null ) {
            return (obj1.equals (obj2));
        } else {
            return (obj1 == obj2);
        }
    }

    
    //
    // class ListMap
    //
    private static class ListMap implements Serializable {
        /** Serial Version UID */
        private static final long serialVersionUID = 6341102578706167575L;

        /** Max length of history. */
        public static final int MAX = 5;
        
        private transient List inputList;
        private transient Map<Object, Object> inputOutputMap;
        /** Serializable mirror of inputList and inputOutputMap fields. */
        private Object[] inputOutputArray;



        public ListMap () {
            init();
        }

        private void init () {
            inputList = new LinkedList();
            inputOutputMap = new HashMap<>();

            if ( inputOutputArray == null ) {
                return;
            }
            for ( int i = 0; i < inputOutputArray.length; i+=2 ) {
                Object input = inputOutputArray[i];
                Object output = inputOutputArray[i+1];

                try { // just hacks to avoid non-String values
                    // check input
                    if ( input instanceof FileObject ) {
                        input = TransformUtil.getURLName ((FileObject) input);
                    } else if ( ( input != null ) &&
                                ( input instanceof String ) == false ) {
                        input = input.toString();
                    }
                    // check output
                    if ( output instanceof FileObject ) {
                        output = TransformUtil.getURLName ((FileObject) output);
                    } else if ( ( output != null ) &&
                                ( output instanceof String ) == false ) {
                        output = output.toString();
                    }

                    inputList.add (input);
                    inputOutputMap.put (input, output);
                } catch (IOException exc) { // TransformUtil.getURLName
                    // ignore it

                    //Util.THIS.debug (exc);
                }
            }
        }

        public void put (String input, String output) {
            // remove old value
            Object old = inputOutputMap.remove (input);
            inputList.remove (input);

            // add new value at first place
            inputOutputMap.put (input, output);
            inputList.add (0, input);
            
            // keep just ${MAX} entries
            if ( inputList.size() > MAX ) {
                Object over = inputList.remove (inputList.size() - 1);
                inputOutputMap.remove (over);
            }
        }

        public String[] getInputs () {
            return (String[]) inputList.toArray (new String[0]);
        }
        
        public String getLastInput () {
            if ( inputList.isEmpty() ) {
                return null;
            }
            return (String) inputList.get (0);
        }

        public String getOutput (String input) {
            return (String) inputOutputMap.get (input);
        }

        public String[] getArray () {
            if ( inputList.size() == 0 ) {
                return null;
            }
            String[] array = new String [2 * inputList.size()];
            for ( int i = 0; i < inputList.size(); i++ ) {
                String input = (String) inputList.get (i);
                array[2*i] = input;
                array[(2*i)+1] = (String) inputOutputMap.get (input);
            }
            return array;
        }

        public String toString () {
            StringBuffer sb = new StringBuffer (super.toString());
            sb.append (" [ inputList= ").append (inputList);
            sb.append (", inputOutputMap.keySet= ").append (inputOutputMap.keySet());
            sb.append (", inputOutputMap.values= ").append (inputOutputMap.values());
            sb.append (", xmlOutputArray= ").append (inputOutputArray == null ? "null" : Arrays.asList (inputOutputArray).toString());
            sb.append (" ]");
            return sb.toString();
        }

        public boolean equals (Object obj) {
            if ( ( obj instanceof ListMap ) == false ) {
                return false;
            }
            ListMap peer = (ListMap)obj;
            if ( TransformHistory.equals (this.inputList, peer.inputList) == false ) {
                return false;
            }
            if ( TransformHistory.equals (this.inputOutputMap, peer.inputOutputMap) == false ) {
                return false;
            }
            return true;
        }
    
        private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();

            init();
            inputOutputArray = null;
        }
        

        private void writeObject (ObjectOutputStream oos) throws IOException {
            inputOutputArray = getArray();
            
            oos.defaultWriteObject();
            
            inputOutputArray = null;
        }
        
    } // class ListMap

}
