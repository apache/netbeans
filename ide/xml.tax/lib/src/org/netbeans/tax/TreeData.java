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
package org.netbeans.tax;

import java.util.StringTokenizer;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class TreeData extends TreeChild {

    /** */
    public static final String PROP_DATA = "data"; // NOI18N

    /** */
    private String data;


    //
    // init
    //

    /**
     * Creates new TreeData.
     * @throws InvalidArgumentException
     */
    protected TreeData (String data) throws InvalidArgumentException {
        super ();

        checkData (data);
        this.data = data;
    }
    
    /** Creates new TreeData -- copy constructor. */
    protected TreeData (TreeData data) {
        super (data);
        
        this.data = data.data;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeData peer = (TreeData) object;
        if (!!! Util.equals (this.getData (), peer.getData ()))
            return false;
        
        return true;
    }
    
    /*
     * Merge data property.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeData peer = (TreeData) treeObject;
        
        try {
            setDataImpl (peer.getData ());
        } catch (Exception exc) {
            throw new CannotMergeException (treeObject, exc);
        }
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final String getData () {
        return data;
    }
    
    /**
     */
    private final void setDataImpl (String newData) {
        String oldData = this.data;
        
        this.data = newData;
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeData::setDataImpl: firing data change " + oldData + " => " +newData); // NOI18N
        
        firePropertyChange (PROP_DATA, oldData, newData);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setData (String newData) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.data, newData) )
            return;
        checkReadOnly ();
        checkData (newData);
        
        //
        // set new value
        //
        setDataImpl (newData);
    }
    
    
    /**
     */
    protected abstract void checkData (String data) throws InvalidArgumentException;
    
    /**
     */
    public final int getLength () {
        return data.length ();
    }
    
    
    /**
     * @throws InvalidArgumentException
     */
    public final String substringData (int offset, int count) throws InvalidArgumentException {
        try {
            return data.substring (offset, offset + count);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidArgumentException (ex);
        }
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void appendData (String appData) throws ReadOnlyException, InvalidArgumentException {
        setData (data + appData);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void insertData (int offset, String inData) throws ReadOnlyException, InvalidArgumentException {
        checkReadOnly ();
        try {
            String preData = data.substring (0, offset);
            String postData = data.substring (offset, data.length ());
            setData (preData + inData + postData);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidArgumentException (ex);
        }
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void deleteData (int offset, int count) throws ReadOnlyException, InvalidArgumentException {
        checkReadOnly ();
        try {
            String preData = data.substring (0, offset);
            String postData = data.substring (offset + count, data.length ());
            setData (preData + postData);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidArgumentException (ex);
        }
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void replaceData (int offset, int count, String repData) throws ReadOnlyException, InvalidArgumentException {
        checkReadOnly ();
        try {
            String preData = data.substring (0, offset);
            String postData = data.substring (offset + count, data.length ());
            setData (preData + repData + postData);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidArgumentException (ex);
        }
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final TreeData splitData (int offset) throws ReadOnlyException, InvalidArgumentException {
        checkReadOnly ();
        TreeData splitedData;
        try {
            String preData = data.substring (0, offset);
            String postData = data.substring (offset, data.length ());
            splitedData = createData (preData);
            setData (postData);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidArgumentException (ex);
        }
        return splitedData;
    }
    
    /**
     * @throws InvalidArgumentException
     */
    protected abstract TreeData createData (String data) throws InvalidArgumentException;
    
    /**
     */
    public final boolean onlyWhiteSpaces () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeData::onlyWhiteSpaces: data = '" + data + "'"); // NOI18N
        
        String trimed = data.trim ();
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("        ::onlyWhiteSpaces: trimed = '" + trimed + "'"); // NOI18N
        
        return (trimed.length () == 0);
    }
    
}
