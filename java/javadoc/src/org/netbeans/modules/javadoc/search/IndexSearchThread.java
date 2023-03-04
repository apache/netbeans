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

package org.netbeans.modules.javadoc.search;

import java.net.URL;
import java.util.StringTokenizer;

import org.openide.util.RequestProcessor;

/** Abstract class for thread which searches for documentation
 *
 *  @author Petr Hrebejk, Petr Suchomel
 */

public abstract class IndexSearchThread implements Runnable  {
    private static final RequestProcessor RP = new RequestProcessor(IndexSearchThread.class.getName(), 1, false, false);

    // PENDING: Add some abstract methods

    //protected String                toFind;

    // documentation index file (or foldee for splitted index)
    protected URL            indexRoot;
    private   DocIndexItemConsumer  ddiConsumer;
    private final RequestProcessor.Task rpTask;
    private boolean isFinished = false;

    protected boolean caseSensitive;
    
    protected String lastField="";     //NOI18N
    protected String middleField="";   //NOI18N    
    protected String reminder="";   //NOI18N
    private int tokens=0;

    private String lastAdd ="";   //NOI18N
    private String lastDeclaring="";   //NOI18N
    /** This method must terminate the process of searching */
    abstract void stopSearch();

    @SuppressWarnings("LeakingThisInConstructor")
    public IndexSearchThread(String toFind, URL fo, DocIndexItemConsumer ddiConsumer, boolean caseSensitive) {
        this.ddiConsumer = ddiConsumer;
        this.indexRoot = fo;
        this.caseSensitive = caseSensitive;
        this.rpTask = RP.create(this);
        
        //this.toFind = toFind;
        //rpTask = RequestProcessor.createRequest( this );

        StringTokenizer st = new StringTokenizer(toFind, ".");     //NOI18N
        tokens = st.countTokens();
        //System.out.println(tokens);
        
        if( tokens > 1 ){
            if( tokens == 2 ){
                middleField = st.nextToken();
                lastField   = st.nextToken();
            }
            else{
                for( int i = 0; i < tokens-2; i++){
                    reminder += st.nextToken();
                    if (i + 1 < tokens - 2) {
                        reminder += '.';
                    }
                }            
                middleField = st.nextToken();
                lastField   = st.nextToken();
            }            
        }
        else{
            lastField = toFind;            
        }
        if( !caseSensitive ){
            reminder    = reminder.toUpperCase();
            middleField = middleField.toUpperCase();
            lastField   = lastField.toUpperCase();
        }
        //System.out.println("lastField" + lastField);
    }

    protected synchronized void insertDocIndexItem( DocIndexItem dii ) {
        //no '.', can add directly
        //System.out.println("Inserting");
        /*
        try{
            PrintWriter pw = new PrintWriter( new FileWriter( "c:/javadoc.dump", true ));
            pw.println("\"" + dii.getField() +"\""+ " " + "\""+dii.getDeclaringClass()+ "\"" + " " + "\""+ dii.getPackage()+ "\"");
            pw.println("\"" + lastField + "\"" + " " + "\"" + middleField + "\"" + " " + "\"" + reminder + "\"");
            pw.flush();
            pw.close();
        }
        catch(IOException ioEx){ioEx.printStackTrace();}
        */
        String diiField = dii.getField();
        String diiDeclaringClass = dii.getDeclaringClass();
        String diiPackage = dii.getPackage();
        if( !caseSensitive ){
            diiField = diiField.toUpperCase();
            diiDeclaringClass = diiDeclaringClass.toUpperCase();
            diiPackage = diiPackage.toUpperCase();
        }
        
        if( tokens < 2 ){
            if( diiField.startsWith( lastField ) ){
                //System.out.println("------");
                //System.out.println("Field: " + diiField + " last field: " + lastAdd + " declaring " + diiDeclaringClass + " package " + diiPackage);
                if( !lastAdd.equals( diiField ) || !lastDeclaring.equals( diiDeclaringClass )){
                    //System.out.println("ADDED");
                    ddiConsumer.addDocIndexItem ( dii );
                    lastAdd = diiField;
                    lastDeclaring = diiDeclaringClass;
                }
                //System.out.println("------");                
            }
            else if( diiDeclaringClass.startsWith( lastField ) && dii.getIconIndex() == DocSearchIcons.ICON_CLASS ) {
                if( !lastAdd.equals( diiDeclaringClass ) ){
                    ddiConsumer.addDocIndexItem ( dii );//System.out.println("Declaring class " + diiDeclaringClass + " icon " + dii.getIconIndex() + " remark " + dii.getRemark());
                    lastAdd = diiDeclaringClass;
                }
            }
            else if( diiPackage.startsWith( lastField + '.' ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ) {
                if( !lastAdd.equals( diiPackage ) ){
                    ddiConsumer.addDocIndexItem ( dii );//System.out.println("Package " + diiPackage + " icon " + dii.getIconIndex() + " remark " + dii.getRemark());
                    lastAdd = diiPackage;
                }
            }
        }
        else{            
            if( tokens == 2 ){
                //class and field (method etc. are equals)
                //System.out.println(dii.getField() + "   " + lastField + "   " + dii.getDeclaringClass() + "   " + middleField);
                if( diiField.startsWith(lastField) && diiDeclaringClass.equals(middleField) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( middleField ) && diiDeclaringClass.equals( lastField ) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( (middleField + '.' + lastField) ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
            }
            else{            
                //class and field (method etc. are equals)
                if( diiField.startsWith(lastField) && diiDeclaringClass.equals(middleField) && diiPackage.startsWith( reminder ) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                //else if( diiDeclaringClass.equals(lastField) && diiPackage.startsWith( (reminder + '.' + middleField).toUpperCase()) ){
                else if( diiDeclaringClass.startsWith(lastField) && diiPackage.equals( (reminder + '.' + middleField + '.')) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( (reminder + '.' + middleField + '.' + lastField) ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
            }
        }
    }

    public void go() {
        rpTask.schedule(0);
        rpTask.waitFinished();
    }

    public void finish() {
        if (!rpTask.isFinished() && !rpTask.cancel()) {
            stopSearch();
        }
        taskFinished();
    }

    public void taskFinished() {
        if (!isFinished) {
            isFinished = true;
            ddiConsumer.indexSearchThreadFinished( this );
        }
    }

    /** Class for callback. Used to feed some container with found
     * index items;
     */

    public interface DocIndexItemConsumer {

        /** Called when an item is found */
        void addDocIndexItem(DocIndexItem dii);

        /** Called when a task finished. May be called more than once */
        void indexSearchThreadFinished(IndexSearchThread ist);

    }

}
