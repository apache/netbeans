"""
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
*
* Contributor(s):
*
* Portions Copyrighted 2009 Sun Microsystems, Inc.
"""

import socket
import dbgutils
import __builtin__
import sys

__author__=""
__date__ ="$Jan 13, 2009 10:03:05 AM$"

class NetworkDebuggingSession :
    """ handle network session for JpyDbg and Completion engine """

    def __init__( self ,  host , port  ) :
        self._lastBuffer = ''
        self._host = host
        self._port = port
        self._jpyutils = dbgutils.JpyUtils()
        self._connected = False
        self._connections = { 'in' : socket.socket( socket.AF_INET , socket.SOCK_STREAM ) ,
                             'out': socket.socket( socket.AF_INET , socket.SOCK_STREAM )
                           }
        self._encoding = sys.stdout.encoding
        if  host == None :
            # listening mode
            print "JPyDbg listening on in=" , port ,"/ out=",port+1
            self._connections['in'].bind( (host,port) )
            self._connections['out'].bind( (host,port+1) )
            self._connections['in'].listen(1)
            self._connections['out'].listen(1)
        else :
            # connectng mode
            print "JPyDbg connecting " , host , " on in= " , port, "/out=",port+1

    def _DBG( self , toWrite ):
        from dbgutils import debugLogger
        if debugLogger :
           debugLogger.debug(toWrite)

    def getNextDebuggerCommand( self ) :
         """ Used by debugger SUSPENDED THREAD to get commands """
         returned = self._receiveCommand()
         self._DBG( "<-- DBG CMD = %s " % (returned) )
         return returned

    def connect( self  )  :
        # start in listen mode waiting for incoming sollicitors
        if self._host == None :
            self._connections['in'] ,addr = self._connections['in'].accept()
            self._connections['out'] ,addr = self._connections['out'].accept()
            print "connected by " , addr
            self._connected = True
        else :
            try:
                self._DBG(">connecting Port  %s "  %  (str(self._port)) )
                self._connections['in'].connect( (self._host , self._port)  )
                self._DBG(">connecting Port  %s"  %  ( str(self._port+1)) )
                self._connections['out'].connect( (self._host , self._port+1)  )
                print "JPyDbgI0001 : connected to " , self._host
                self._connected = True
            except socket.error, (errno,strerror):
                print "ERROR:JPyDbg connection failed errno(%s) : %s" % ( errno , strerror )
        return self._connected

    def readNetBuffer( self ):
        """ reading on network socket """
        try:
            if ( self._lastBuffer.find('\n') != -1 ):
                return self._lastBuffer ; # buffer stills contains commands
            networkData = self._connections['in'].recv(1024)
            if not networkData:  # capture network interuptions if any
                return None
            data = self._lastBuffer + networkData
            return data
        except socket.error, (errno,strerror):
            print "recv interupted errno(%s) : %s" % ( errno , strerror )
            return None

    def _receiveCommand( self ):
        """ receive a command back """
        # self._DBG(">Wait on NET ... ")
        data = self.readNetBuffer() ;
        # data reception from IP session
        while ( data != None and data):
            eocPos = data.find('\n')
            nextPos = eocPos ;
            while (  nextPos < len(data) and \
                   ( data[nextPos] == '\n' or data[nextPos] == '\r') ): # ignore consecutive \n\r
                nextPos = nextPos+1
            if ( eocPos != -1 ): # full command received in buffer
                self._lastBuffer = data[nextPos:] # cleanup received command from buffer
                returned = data[:eocPos]
                # self._DBG( "<-- received Command " + returned )
                if (returned[-1] == '\r'):
                    return returned[:-1]
                return returned
            data = self.readNetBuffer() ;
        # returning None on Ip Exception
        return None

    def _send(self , buffer ):
        # we need to wait for socket ready here
        if self._connected :
            self._connections['out'].send( buffer )
            self._DBG( "sent --> " + buffer )

    def populateToClient( self , bufferList ) :
        """ populate back bufferList to client side """
        self._DBG( "populateXmlToClient --> " + buffer )
        self._send( ''.join(bufferList) )

    def populateXmlToClient( self , bufferList ) :
        """ populate JpyDbg Xml buffer back """
        mbuffer = '<JPY>'
        for element in bufferList:
            if isinstance(element, unicode):
                elm = element.encode(self._encoding)
            else:
                elm = str(element)
            mbuffer = mbuffer + ' ' + elm
        mbuffer = mbuffer + '</JPY>\n'
        self._DBG( "populateToClient --> " + mbuffer )
        self._send( mbuffer )

    def populateCommandToClient( self , command , result ):
        """ send a command result back """
        self.populateXmlToClient( [ '<' + result[0] ,
                               'cmd="' + self._jpyutils.removeForXml(command) +'"' ,
                               'operation="' + self._jpyutils.removeForXml(__builtin__.str(result[1]))+'"' ,
                               'result="' + __builtin__.str(result[2])+'"' ,
                               '/>' ] )
        if ( result[3] != None ):
            for element in result[3]:
                self.populateXmlToClient( [ '<COMMANDDETAIL ' ,
                      'content="'+ self._jpyutils.removeForXml(element)+'"',
                                       ' />'
                                      ]
                                    )
        # complementary TAG may be provided starting at position 4
        if len(result) > 4 and (result[4]!=None):
            self.populateXmlToClient( result[4] )
        # mark the end of <COMMANDDETAIL> message transmission
        self.populateXmlToClient( [ '<COMMANDDETAIL/>' ] )

    def populateDebugTermination( self ):
        self.populateXmlToClient( [ "<DEBUG result='ENDED' />" ] )


    def terminate( self ):
        """ close the associated ip session """
        self._DBG( "**** DEBUGGER CONNECTION CLOSED ***" )
        if self._connected :
            self._connections['in'].close()
            self._connections['out'].close()
            self._connected = False


