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
