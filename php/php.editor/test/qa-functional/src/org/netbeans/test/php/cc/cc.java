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

package org.netbeans.test.php.cc;

import org.netbeans.test.php.GeneralPHP;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class cc extends GeneralPHP
{
  public cc( String arg0 )
  {
    super( arg0 );
  }

  protected static final int DOLLAR_COMPLETION_LIST = 16;
  protected static final int SLASHSTAR_COMPLETION_LIST = 220;
  protected static final int JAVADOC_COMPLETION_LIST = 50;

/*
    protected CompletionJListOperator GetCompletion( )
    {
      CompletionJListOperator comp = null;
      int iRedo = 10;
      while( true )
      {
        try
        {
        comp = new CompletionJListOperator( );
        try
        {
          Object o = comp.getCompletionItems( ).get( 0 );
          if(
              !o.toString( ).contains( "No suggestions" )
              && !o.toString( ).contains( "Scanning in progress..." )
            )
          {
            return comp;
          }
          Sleep( 1000 );
        }
        catch( java.lang.Exception ex )
        {
          return null;
        }
        }
        catch( JemmyException ex )
        {
          System.out.println( "Wait completion timeout." );
          if( 0 == --iRedo )
            return null;
        }
        Sleep( 100 );//try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) {}
      }
    }
*/

/*
    protected void CheckCompletionItems(
        CompletionJListOperator jlist,
        String[] asIdeal
      )
    {
      for( String sCode : asIdeal )
      {
        int iIndex = jlist.findItemIndex( sCode );
        if( -1 == iIndex )
        {
          try
          {
          List list = jlist.getCompletionItems();
          for( int i = 0; i < list.size( ); i++ )
            System.out.println( "******" + list.get( i ) );
          }
          catch( java.lang.Exception ex )
          {
            System.out.println( "#" + ex.getMessage( ) );
          }
          fail( "Unable to find " + sCode + " completion." );
        }
      }
    }
*/

  protected void WaitCompletionScanning( )
  {
    // Wait till disappear or contains more then dummy item
    CompletionJListOperator comp = null;
    while( true )
    {
      try
      {
        comp = new CompletionJListOperator( );
        if( null == comp )
          return;
        try
        {
          Object o = comp.getCompletionItems( ).get( 0 );
          if(
              !o.toString( ).contains( "No suggestions" )
              && !o.toString( ).contains( "Scanning in progress..." )
            )
          {
            return;
          }
          Sleep( 100 );
        }
        catch( java.lang.Exception ex )
        {
          return;
        }
      }
      catch( JemmyException ex )
      {
        return;
      }
      Sleep( 100 );
    }
  }

}
