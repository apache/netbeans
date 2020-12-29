#! /usr/bin/env python
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

""" this daemon may be used by 'external' python sollicitors interfaces """
__version__='$Revision: 1.4 $'
__date__='$Date: 2006/11/25 15:23:48 $'
__author__ = ""



import sys
import bdb
import string
import traceback
import threading
import os
import types
import __builtin__
import dbgutils
import weakref
import dbgnetwork

#from dbgutils import *

HOST = ''
PORT = 29000 # default listening port
OK   = "OK"

COMMAND = 0
SET_BP  = 2
DEBUG   = 31
STEP    = 4
NEXT    = 5
RUN     = 6
FREEZE  = 7 # remain on current line
CLEAR_BP  = 8
STACK   = 9
QUIT    = 10
LOCALS  = 11
GLOBALS = 12
SETARGS = 13
READSRC = 14
COMPOSITE = 15
THREAD = 16
STEP_RETURN = 17
UNKNOWN = -1

# Thread running states
STATE_RUNNING = 0
STATE_SUSPENDED = 1
STATE_SUSPENDING = 2
STATE_RESUMING = 3
STATE_ONBREAKPOINT = 4

# Breakpoint hits
HIT_NOT_SET = -1
HIT_EQUALS_TO = 0
HIT_GREATER_THAN = 1
HIT_MULTIPLE_OF = 2

CP037_OPENBRACKET='\xBA'
CP037_CLOSEBRACKET='\xBB'

# internal threads naming
STARTER_THREAD_NAME = "StarterThread"


def SetTraceForParents(frame, dispatch_func):
    frame = frame.f_back
    while frame:
        frame.f_trace = dispatch_func
        frame = frame.f_back
    del frame

# get jpyutils global instance
_utils = dbgutils.jpyutils

class UntracedSources :
    """ singleton class to just prevent system python sources tracing from beeing enabled"""
    def __init__( self ) :
        self.DONT_TRACE = {
              #commonly used things from the stdlib that we don't want to trace
              'atexit.py':1,
              'codecs.py':1,
              'threading.py':1,
              'Queue.py':1,
              'socket.py':1,
              'bdb.py':1,
              'string.py':1,
              'linecache.py':1,
              'weakref.py':1,
              'sre_parse.py':1,
              'sre_compile.py':1,
              'string.py':1,
              'UserDict.py':1,
              #things from jpydbg that we don't want to trace
              'dbgnetwork.py':1,
              'jpydaemon.py':1,
              'dbgutils.py':1
        }

        # set threading trace
#        self.DEBUGGERTHREADS = {
 #             GLOBAL_COMMANDER_THREAD_NAME:1 ,
  #            NETWORK_THREAD_NAME:1
 #                                  }

    def isTraced( self , source ) :
        if self.DONT_TRACE.has_key(source):
            return False
        return True

#    def isDebuggerThread(self,name):
#        if self.DEBUGGERTHREADS.has_key(name) :
#            return True
 #       return False

_checkTraced = UntracedSources()

isJython = False
try :
    import java.lang
    isJython = True
except :
    pass

def _DEBUG(   message) :
    # DEBUG TRACING when things goes wrong
    from dbgutils import debugLogger
    if debugLogger != None :
        debugLogger.debug(message)

class JpyDbgBreakpoint(bdb.Breakpoint):
    """ override BdbBreakpoint to support hits """

    def __init__(self , file, line, temporary=0, cond = None , hits = 0 , hitStyle = HIT_NOT_SET ) :
        bdb.Breakpoint.__init__(self ,file,line,temporary,cond )
        self.jpyhits = hits
        self.hitStyle = hitStyle
        self.hitted = 0

    def bpprint(self):

        if self.temporary:
            disp = 'del  '
        else:
            disp = 'keep '
        if self.enabled:
            disp = disp + 'yes'
        else:
            disp = disp + 'no '
        returned = '%-4dbreakpoint    %s at %s:%d' % (self.number, disp,
                             self.file, self.line)
        if self.cond:
            returned= returned + '\tstop only if %s' % (self.cond,)
        if self.ignore:
            returned= returned +  '\tignore next %d hits' % (self.ignore)
        if (self.jpyhits):
            if (self.hitted > 1): ss = 's'
            else: ss = ''
            returned= returned +  ('\tbreakpoint already hit %d time%s' %
                   (self.hitted, ss))
        return returned


class BdbClone(bdb.Bdb) :

    def __init__( self ) :
        bdb.Bdb.__init__( self)
        self.encoding = sys.stdout.encoding
        self.lock = threading.Lock()
        self._acquire_lock = self.lock.acquire
        self._release_lock = self.lock.release
        self.running =  False
        # hide is used to prevent debug inside debugger
        self.hide = 0
        # store debugger script name to avoid debugging it's own frame
        #self.debuggerFName = os.path.normcase(sys.argv[0])
        self.debuggerFName = os.path.normcase(sys._getframe(0).f_code.co_filename)
        print self.debuggerFName
        # client debugger connection
        self._connection = None
        # debugger active information
        self.debuggee = None
        self.cmd = UNKNOWN
        # net buffer content
        self.lastBuffer = ""
        # EXCEPTION raised flag
        self.exceptionRaised = 0
        # EXCEPTION infos
        self.exceptionInfo = None
        # debuggee current 'command line arguments'
        self.debuggeeArgs = None
        # inside a command
        self._inCommand = 0
        # last executed line exception or None
        self.lastLineException = None
        # on going running
        self.running_threads= {}

    def addThreadFrame(self , thread , runningFrames):
        self._acquire_lock()
        try:
          if not thread in self.running_threads :
              # populate
              self.running_threads[thread] = runningFrames
        finally:
            self._release_lock()
    #
    # handle EBCDIC MVS idiosynchrasies
    #
    def _mvsCp037Check( self , inStr ):
        if sys.platform != 'mvs' :
            return inStr
        inStr = inStr.replace('[',CP037_OPENBRACKET)
        inStr = inStr.replace(']',CP037_CLOSEBRACKET)
        return inStr


    def sendBack( self , message ) :
        """ just populate back over wire """
        self._connection.populateXmlToClient(message)

    def receiveCommand( self ) :
        """ get the command over the wire """
        return self._connection.receiveCommand()

    def run(self, cmd , myglobals=None, mylocals=None):
        """A copy of bdb's run but with a local variable added so we
        can find it it a call stack and hide it when desired (which is
        probably most of the time).
        """
        if myglobals is None:
            import __main__
            myglobals = __main__.__dict__
        if mylocals is None:
            mylocals = myglobals
        self.reset()
        if not isinstance(cmd, types.CodeType):
            cmd = cmd+'\n'
        self.running = True
        threadList = None
        try:
            _DEBUG("*** ENTERING EXEC *** ")
            # exec cmd in myglobals,mylocals
            mainthread = MainThread(self,cmd,myglobals,mylocals)
            mainthread.start()
            # mainthread.join() # wait for userapp mainthread completion
            _DEBUG("*** RETURNING EXEC ***")
            threadList = self.listThreads()
            for threadElement in threadList:
                _DEBUG("thread= " +threadElement[1] )

        except dbgutils.JpyDbgQuit:
            _DEBUG("*** quiting exception raised (forced debuggee quit)")
            # if exceptionInfo is None => we're leaving due to USER STOP REQUEST => just leave debugger
            #if ( self.exceptionInfo != None ) :
                # exception raised in debuggee show it back to client with debuggee error's frame
            #    _DEBUG("*** exception infor =%s " % (str(self.exceptionInfo) ) )
            #    self.populate_exception(self.exceptionInfo)
        if threadList == None or len(threadList) == 0  :
            self.running = False # no more active Threads

    def _discardCallFrame( self , fName ) :
        """ prevent populating debugger frame back """
        if fName == self.debuggerFName or self.hide:
            self.hide = self.hide + 1
        if self.hide:
            return True
        return False

    def _debuggerContext( self , fName ) :
        # let's assume that all file located in same directory as jpydaemon
        # are debugger's modules looks reasonable
        if os.path.dirname(fName) == os.path.dirname(self.debuggerFName) :
            return True
        return False

#    def trace( self , message ):
#      self.dbgTrc.write(message + '\n');
    def do_clear(self, arg):
        pass

    # bdb overwitten to capture call debug event
    def user_call(self, frame, args):
        name = frame.f_code.co_name
        if not name: name = '???'
        fn = self.canonic(frame.f_code.co_filename)
        if not fn:
            fn = '???'
        if not self._discardCallFrame(fn) :
            self.sendBack( [ '<CALL',
                                 'cmd="'+ __builtin__.str(self.cmd)+'"' ,
                                 'fn="'+ _utils.removeForXml(fn) +'"' ,
                                 'name="'+_utils.removeForXml(name)+'"',
                                 'args="'+__builtin__.str(args)+'"' ,
                                 '/>' ]
                             )

    # bdb overwitten to capture line debug event
    def user_line(self, frame):
        import linecache
        name = frame.f_code.co_name
        if not name: name = '???'
        fn = self.canonic(frame.f_code.co_filename)
        if not fn: fn = '???'
        # populate info to client side
        line = linecache.getline(fn, frame.f_lineno)
        xmlLine =  [ '<LINE',
                               'cmd="'+ __builtin__.str(self.cmd)+'"' ,
                               'fn="'+ _utils.removeForXml(fn)+'"' ,
                               'lineno="'+__builtin__.str(frame.f_lineno)+'"' ,
                               'name="' + _utils.removeForXml(name) + '"' ,
                               'line="' + _utils.removeForXml(line.strip())+'"',
                               '/>']
        self.sendBack( xmlLine )

    def send_client_exception( self , cmd , content ):
        # self.trace("exception sent")
        self.sendBack( ['<EXCEPTION',
                               'cmd="'+cmd+'"' ,
                               'content="'+self._mvsCp037Check(content)+'"' ,
                              '/>'] )

    def populate_exception( self , exc_stuff):
        # self.trace("exception populated")
        if ( self.exceptionRaised == 0 ): # exception not yet processed
            _DEBUG("*** exc_stuff = %s" % (str(exc_stuff) ) )
            extype  = exc_stuff[0]
            value = exc_stuff[1]
            tb  = exc_stuff[2]
            excTrace = __builtin__.str( traceback.format_exception( extype , value , tb ) )

            #ex = exc_stuff
            # Deal With SystemExit in specific way to reflect debuggee's return
            if issubclass( extype , SystemExit):
                content = 'System Exit REQUESTED BY DEBUGGEE  =' + str(value)
            elif issubclass(extype, SyntaxError):
                content = __builtin__.str(value)
                error = value[0]
                compd = value[1]
                content = 'SOURCE:SYNTAXERROR:"'+\
                       __builtin__.str(compd[0])+ '":('+\
                       __builtin__.str(compd[1])+','+\
                       __builtin__.str(compd[2])+\
                       ')'+':'+error
            elif issubclass(extype,NameError):
                content = 'SOURCE:NAMEERROR:'+__builtin__.str(value)
            elif issubclass(extype,ImportError):
                content = 'SOURCE::IMPORTERROR:'+__builtin__.str(value)
            else:
                content = __builtin__.str(value)
            # keep track of received exception
            # populate exception
            self.lastLineException = ['<EXCEPTION',
                                    'cmd="'+__builtin__.str(self.cmd)+'"' ,
                                    'content="'+ _utils.removeForXml(content)+ \
                                    '"' ,
                                    '/>']
            self.send_client_exception( __builtin__.str(self.cmd) , _utils.removeForXml(content+excTrace) )
            self.exceptionRaised = 1 # set ExceptionFlag On

    # bdb overwitten to capture return debug event
    def user_return(self, frame, retval):
        fn = self.canonic(frame.f_code.co_filename)
        if not fn: fn = '???'
        if self.hide:
            self.hide = self.hide - 1
            return None
        self.sendBack( [  '<RETURN',
                                  'cmd="'+__builtin__.str(self.cmd)+'"' ,
                                  'fn="'+ _utils.removeForXml(fn)+'"' ,
                                  'retval="'+_utils.removeForXml(__builtin__.str(retval))+'"' ,
                                  '/>'] )


    # acting as stdin for commands => redirect read to the wire
    def readline( self ):
        command = self._connection.readNetBuffer()
        if self._inCommand :
            verb , mhelp = self.commandSyntax( command )
            return mhelp
        return command[4:] # bypass CMD header for debuggee user input


    # acting as stdout => redirect to client side
    def write( self , toPrint ):
        # transform eol pattern
        if ( toPrint == "\n" ):
            toPrint = "/EOL/"
        self._connection.populateXmlToClient( ['<STDOUT' , 'content="'+ _utils.removeForXml(toPrint)+'"' , '/>' ] )

    # acting as stdout => redirect to client side
    def writeline( self , toPrint ):
        # stdout redirection
        self.write(toPrint )
        self.write("\n")

      # stdout flush override
    def flush( self ):
        pass

    def listThreads( self ) :
        returned = []
        threads = threading.enumerate()
        for t in threads:
            if hasattr(t , 'additionalInfo') :
               # check for living frames for given Thread
               if t.additionalInfo.dbFrames.isActive()  :
                   returned.append( ( "P" ,  t.getName()  )  )
                   _DEBUG(  '%s is an active THREAD '  % ( str(t))  )
               else :
                   _DEBUG(  'No ExtraInfos for THREAD : %s '  % ( str(t))  )
        return returned


    def isDead(self , threadId):
        threads = threading.enumerate()
        for t in threads :
            if t == threadId :
               if hasattr(t , 'additionalInfo') :
                   # check for living frames for given Thread
                   return not t.additionalInfo.dbFrames.isActive()
               else :
                  _DEBUG(  'No ExtraInfos for THREAD : %s '  % ( str(t)))
        return False

    def set_break(self, filename, lineno, temporary=0, cond = None , hits = 0 , hitStyle = -1):
        """ Overridden bdb set_break """
        filename = self.canonic(filename)
        import linecache # Import as late as possible
        line = linecache.getline(filename, lineno)
        if not line:
            return 'Line %s:%d does not exist' % (filename,
                                   lineno)
        if not self.breaks.has_key(filename):
            self.breaks[filename] = []
        list = self.breaks[filename]
        if not lineno in list:
            list.append(lineno)
        bp = JpyDbgBreakpoint(filename, lineno, temporary, cond , hits , hitStyle )

    def _checkHit( self , bp ):
         """ deal with breakpoint hits checking """
         hit = 0
         if bp.jpyhits != 0 :
             # check hits context
             bp.hitted = bp.hitted + 1
             if bp.hitStyle == HIT_EQUALS_TO and \
                bp.hitted == bp.jpyhits :
                    hit = 1
             elif bp.hitStyle == HIT_GREATER_THAN and \
                 bp.hitted > bp.jpyhits :
                    hit = 1
             elif bp.hitStyle == HIT_MULTIPLE_OF and \
                 bp.hitted % bp.jpyhits == 0 :
                    hit = 1
             return hit
         else :
             return 1

    # Determines if there is an effective (active) breakpoint at this
    # line of code.  Returns breakpoint number or 0 if none
    def effective( self , file, line, frame):
        """Determine which breakpoint for this file:line is to be acted upon.

        Called only if we know there is a bpt at this
        location.  Returns breakpoint that was triggered and a flag
        that indicates if it is ok to delete a temporary bp.

        """
        possibles = JpyDbgBreakpoint.bplist[file,line]
        for i in range(0, len(possibles)):
            b = possibles[i]
            _DEBUG("check effective : %s" % ( b.bpprint() ))
            if b.enabled == 0:
                continue
            if not b.cond:
                # If unconditional, and ignoring,
                # go on to next, else break
                if b.ignore > 0:
                    b.ignore = b.ignore -1
                    continue
                else:
                    # breakpoint and marker that's ok
                    # to delete if temporary
                    return (b,1)
            else:
                # Conditional bp.
                # Ignore count applies only to those bpt hits where the
                # condition evaluates to true.
                try:
                    val = eval(b.cond, frame.f_globals,
                           frame.f_locals)
                    _DEBUG("eval condition %s in context %d" % (b.cond,val))
                    if val:
                        if b.ignore > 0:
                            b.ignore = b.ignore -1
                            # continue
                        else:
                            return (b,1)
                    # else:
                    #   continue
                except:
                    # if eval fails, most conservative
                    # thing is to stop on breakpoint
                    # regardless of ignore count.
                    # Don't delete temporary,
                    # as another hint to user.
                    return (b,0)
        return (None, None)


    def break_here(self, frame):
         """ overridden bdb break_here """
         _DEBUG("entering break_here")
         filename = self.canonic(frame.f_code.co_filename)
         if not self.breaks.has_key(filename):
             return 0
         lineno = frame.f_lineno
         if not lineno in self.breaks[filename]:
             return 0
         _DEBUG("found in break table")
         # flag says ok to delete temp. bp
         (bp, flag) = self.effective(filename, lineno, frame)
         if bp:
             _DEBUG("effective BREAK")
             self.currentbp = bp.number
             if (flag and bp.temporary):
                 self.do_clear(str(bp.number))
             # finally deal wit hits
             _DEBUG("chkiHit : hitted=%d  hitsExpect=%d"  % ( bp.hitted , bp.jpyhits) )
             return self._checkHit(bp)
         else:
             _DEBUG("effective UNBREAK")
             return 0

class MainThread (threading.Thread) :
    """ debuggee mainthread """
    def __init__(self , debugger,  cmd , myglobals ,mylocals ):
        threading.Thread.__init__( self , name='MainThread')
        self._cmd = cmd
        self._myglobals = myglobals
        self._mylocals = mylocals
        self._debugger = debugger
        # set debuggng traces
        self.dbgTracer = JpyDbgTracer(self._debugger)
        # now started
        debugger.startInProgress = False
        # sys.settrace ( self.dbgTracer.trace_dispatch )

    def run ( self ) :
        """ just exec user module in MainThread """
        exec self._cmd in self._myglobals,self._mylocals


class JPyDbgFrame :

    def __init__(self, *args):
        #args = mainDebugger, filename, base, info, t, frame
        #faster way of getting args follow
        self._args = args[:-1]
        self._botFrame = args[-1] # store the bottom frame of thread (last instruction)

    def dispatchLineAndBreak( self , debugger , frame  , lthread) :
        debugger.user_line(frame)
        _DEBUG( 'THREAD Dispatch before checkdbgAction')
        lthread.additionalInfo.breakHere(frame,lthread)


    def printDispatchContext( self , event , frame ) :
        name = frame.f_code.co_name
        fn = frame.f_code.co_filename
        if not fn: fn = '???'
        # populate info to client side
        line =  frame.f_lineno
        backFrameLine=-1
        if frame.f_back != None :
            backFrameLine = frame.f_back.f_lineno
        _DEBUG(  (' THREAD Dispatch  entering :  event=%s , fname=%s ,  name =%s , line=%i , backframeline=%i') % (event,fn,name,line , backFrameLine)  )

    def checkForBeakpoint( self , mainDebugger , frame , fileName , lineNumber , lthread) :
        # just check that we reached a breakpoint in RUN mode
        _DEBUG( 'check bp  for file %s on line %i'   % (fileName,lineNumber) )
        if mainDebugger.break_here(frame)  :
            # break point have been reached => stop
            _DEBUG( 'BREAKPOINT reached')
            mainDebugger.dbgContinue = False  # continue suspended
            self.dispatchLineAndBreak(mainDebugger, frame,lthread)

    def discardedException( self,  exc_stuff  ):
        """ discard here any intermediate exception that may be received as event """
        extype  = exc_stuff[0]
        value = exc_stuff[1]
        tb  = exc_stuff[2]
        if isJython  :
            # Misc intermediate ImportError exceptions may occur
            # during Jython import we need to ignore them ; if a module can't be
            # really imported a final ImportError will break the execution of the debuggee
            # so it's safe to ignore ImportError for Jythn here
            if  issubclass(extype,ImportError) :
                return True # discarded
        return False


    def trace_dispatch(self, frame, event, arg):
        """ debugger dispatching entry point """

        # trace Frame on entry
        self.printDispatchContext( event, frame )

        if event not in ('line', 'call', 'return', 'exception'):
            return None

        mainDebugger, filename, info, lthread = self._args

        fileName = frame.f_code.co_filename
        lineNumber = frame.f_lineno

        if fileName == '<string>' :
            # just discard those frames
            return None

        _DEBUG(  ' THREAD Dispatch  Second:  %s , %s '   % (  id(lthread) , event  )  )
        if event == 'call':
            # just dispatch to client side for any interest
            _DEBUG( 'THREAD Dispatch before dispatch call fname,debuggee %s,%s' % (fileName,mainDebugger.debuggee))
            returned = mainDebugger.dispatch_call(frame, arg)
            if fileName == mainDebugger.debuggee :
                return self.trace_dispatch
            return returned

        if event == 'return':
            # just dispatch to client side for any interest
            _DEBUG( 'THREAD Dispatch before dispatch return , info.cmd=%s' %( str(info.cmd) ) )
            mainDebugger.dispatch_return(frame, arg)
            if info.stop_frame != None and info.stop_frame == frame.f_back :
                # set STOP in next backFrame
                info.stop_frame = info.stop_frame.f_back
            return self.trace_dispatch

        if event == 'exception':
            # set user exception info to be able to populate to client side later
            # mainDebugger.user_exception(frame,arg)
            if not self.discardedException(arg) :
                mainDebugger.populate_exception(arg)
                mainDebugger.setTrace(None)
                # leave debuggee
                # raise BdbQuit
                mainDebugger.terminateDaemon()
                return None # stop trace on current frame
            return self.trace_dispatch


        # return  mainDebugger.trace_dispatch(frame , event , arg)
        #if event == 'line':
        # ASSUME LINE FOR Jython swing
        _DEBUG( 'THREAD Dispatch before dispatch line , info.cmd=%s' %( str(info.cmd) )  )
        info.last_line_frame = frame # store last thread instruction
        if ( not mainDebugger.dbgContinue  )   :
            #  not in RUN/CONTINUE
            _DEBUG('not CONTINUE' )
            # => handle NEXT stop in next line in current frame
            # => where for thread in NEXT context + should bypass CALLED frames
            if ( info.cmd == NEXT  )   :  # THREAD in NEXT
                # prevent stepping in children's frame or after a 'return' event
                if info.stop_frame  == frame.f_back   :
                    # next statement reached
                    if info.stop_frame != None :
                        _DEBUG( 'NEXT reached , expectedbackframeline=%i' % (info.stop_frame.f_lineno) )
                    info.cmd = None
                    self.dispatchLineAndBreak(mainDebugger, frame , lthread )
                # check for breakpoints as well
                else :
                    _DEBUG( "before checkForBreakPoint call" )
                    self.checkForBeakpoint(mainDebugger,frame,fileName,lineNumber,lthread)
                # return trace fx in any cases
                return self.trace_dispatch
            else :
                #  DEBUG starting , STEP command => stop on next line
                #_DEBUG( 'info.cmd=% '  % (info.cmd) )
                #  thread frame in STEP INTO/OUT or DEBUG initial start
                _DEBUG('before STEP(%s) or DEBUG(%s)' % (info.cmd,mainDebugger.cmd) )
                if  info.cmd == STEP or \
                  ( mainDebugger.isStarting() ) :
                    _DEBUG( 'STEP reached')
                    info.cmd = None
                    self.dispatchLineAndBreak(mainDebugger, frame , lthread )
                elif info.cmd == STEP_RETURN  and info.step_out != None :
                    _DEBUG( "check stepReturn %s==%s" % (info.step_out.f_lineno , frame.f_lineno)     )
                    if frame == info.step_out :
                        self.dispatchLineAndBreak(mainDebugger, frame , lthread )

                return self.trace_dispatch
        else :
            # just check that we reached a breakpoint in RUN mode
            self.checkForBeakpoint(mainDebugger,frame,fileName,lineNumber,lthread)
        # return trace fx in any cases
        return self.trace_dispatch
        #return None



class Suspended :
    """ Threads Suspend / resume manager class"""
    def __init__( self ):
        self._suspended = {}
        self._lock = threading.Lock()

    def setSuspended( self , curThread  ) :
        try :
            self._lock.acquire()
            self._suspended[curThread] = curThread
        finally :
            self._lock.release()


    def resume(self , curThread ):
        try :
            self._lock.acquire()
            if ( self._suspended.has_key(curThread) ) :
              del self._suspended[curThread]
        finally :
            self._lock.release()

    def listSuspended(self) :
        try :
            self._lock.acquire()
            returned = self._suspended.keys()
        finally :
            self._lock.release()
        return returned

    def isSuspended( self , curThread ):
        self._lock.acquire()
        suspended = self._suspended.has_key(curThread)
        self._lock.release()
        return suspended

class ExtraThreadInfos :
    """
    used to provide complementary debugging context for standard PythonThreads object
    including a tracing routine for all threads
    """
    def __init__ (self , dbg , threadid ) :
        self.stop_frame = None
        self.last_line_frame = None
        self.cmd = None
        self.notify_kill = False
        self.step_out = None
        # safely clear event to wait when setSuspended is entered
        self._state = STATE_RUNNING
        self.dbg = dbg
        self.threadId = threadid
        self.dbFrames = ThreadFrames(self.dbg,threadid)

    def setSuspended( self  ):
        self._state = STATE_SUSPENDED
        # put in suspended table
        self.dbg._suspended.setSuspended(self.threadId)
        # TODO Wait for resume on Queue to be implemented
        # self._messageQ.get()

    def resume( self ):
        # self._messageQ.put('RESUME')
        # remove from suspended table
        self.dbg._suspended.resume(self.threadId)
        self._state = STATE_RUNNING

    def breakHere( self , frame , lthread ) :
        self._state = STATE_ONBREAKPOINT
        self.dbg._suspended.setSuspended(self.threadId)
        try :
            while ( self.dbg.parseSubCommand(  self.dbg._connection.getNextDebuggerCommand() , frame , lthread ) == FREEZE ):
               pass
            self.dbg._suspended.resume(self.threadId)
            self._state = STATE_RUNNING
        except dbgutils.JpyDbgQuit :
            # just leave
            self.dbg.terminateDaemon()


    def CreateDbFrame(self, mainDebugger, filename, additionalInfo, t, frame):
        #the frame must be cached as a weak-ref (we return the actual db frame -- which will be kept
        #alive until its trace_dispatch method is not referenced anymore).
        #
        db_frame = JPyDbgFrame(mainDebugger, filename, additionalInfo, t, frame)
        db_frame.frame = frame
        # populate current frame to thread frames
        self.dbFrames.addDbFrame(db_frame)
        return db_frame

    def __str__(self):
        return 'Stop:%s Cmd: %s Kill:%s ' % ( self.stop_frame, self.cmd, self.notify_kill)


class ThreadFrames :
    """ dynamically manage living frames associated with a given Thread """

    def __init__(self , debugger , threadId):
        self.lock = threading.Lock()
        self._acquire_lock = self.lock.acquire
        self._release_lock = self.lock.release
        self._debugger = debugger
        #collection with the refs
        d = {}
        self.existing_frames = d
        # keep in debugger tables for each thread
        debugger.addThreadFrame(threadId ,d)
        try:
            self._iter_frames = d.iterkeys
        except AttributeError:
            self._iter_frames = d.keys

    def isActive(self):
        """ at least one frame reminding """
        self._acquire_lock()
        returned = True
        try:
            if len(self.existing_frames) == 0 :
                returned = False
        finally:
            self._release_lock()
        return returned

    def _OnDbFrameCollected(self, ref):
        '''
            Callback to be called when a given reference is garbage-collected.
        '''
        self._acquire_lock()
        try:
            del self.existing_frames[ref]
            # activate debugger termination when no more frames in progress
            #if self._debugger.isDebuggingOver() :
            #    self._debugger.terminateDaemon()
        finally:
            self._release_lock()

    def addDbFrame(self, db_frame):
        self._acquire_lock()
        try:
            #create the db frame with a callback to remove it from the dict when it's garbage-collected
            #(could be a set, but that's not available on all versions we want to target).
            r = weakref.ref(db_frame, self._OnDbFrameCollected)
            self.existing_frames[r] = r
        finally:
            self._release_lock()


#===================================================
# just host the main JpyDbg debug trace main hook
#===================================================
class JpyDbgTracer :

    def __init__( self  ,  dbg ) :
        self._dbg  =  dbg
        _DEBUG( "Entering JpyDbgTracer , CMD=%s" % (dbg.cmd) )
        self._running_threads_ids = {}
        self._dbg.setTrace(self.trace_dispatch)


    def processThreadNotAlive(self, threadId):
        """ if thread is not alive, cancel trace_dispatch processing """
        mythread = self._running_threads_ids.get(threadId, None)
        if mythread is None:
            return
         # remove from threadlists
        del self._running_threads_ids[threadId]
        # TODO : cancel thread trace_dispatch processing



    def trace_dispatch(self, frame, event, arg):
        "''' This is the callback used when we enter some context in the JpyDbg debugger """

        try:
            # capture the current thread info which goes with received stack
            t = threading.currentThread()
            # Do not Trace debugger Threads
            #if _checkTraced.isDebuggerThread(t.getName() ) :
            #    return None

            f = frame.f_code.co_filename

            filename, base = os.path.split(f)
            if not _checkTraced.isTraced(base) :
                #we don't want to debug threading or anything in jpydbg code
                return None

            _DEBUG(  '**** NEW FRAME : trace_dispatch :  frame < base %s ,lineno %s ,event %s ,code %s>' % ( base, frame.f_lineno, event, frame.f_code.co_name) )


            # if thread is not alive, cancel trace_dispatch processing
            if not t.isAlive():
                self.processThreadNotAlive(id(t))
                return None # suspend tracing

            if not hasattr( t , 'additionalInfo' ) :
                t.additionalInfo = additionalInfo = ExtraThreadInfos( self._dbg , t)
            else :
                additionalInfo = t.additionalInfo
            #always keep a reference to the topmost frame so that we're able to start tracing it (if it was untraced)
            #that's needed when a breakpoint is added in a current frame for a currently untraced context.

            #each new frame...
            dbFrame = additionalInfo.CreateDbFrame(self._dbg, filename, additionalInfo, t, frame)
            return dbFrame.trace_dispatch(frame, event, arg)
        except:
            traceback.print_exc()
            return None

#===================================================
# Main debugging frontend class
#===================================================
class JPyDbg(BdbClone) :

    def __init__(self):
        BdbClone.__init__(self)
        # frame debuggee contexts
        self.globalContext = None
        self.localContext = None
        self.verbose = 0
        self.dbgTracer =  None
        self.stdout = sys.stdout
        self.stdin = sys.stdin
        self.dbgstdout = self
        self.dbgstdin = self
        self.dbgContinue = False
        # suspend/resume debugger thread manager
        self._suspended = Suspended()
        self._verb = None
        self._starting = True
        self._networkSession = None
        self.startInProgress = False

    def isStarting(self ):
        """ simply used to detect first incoming frame to debug """
        if self._starting :
            self._starting = False
            return True
        return False

    def parsedReturned( self , command = 'COMMAND' , argument = None , message = None , details = None ):
        parsedCommand = []
        parsedCommand.append(command)
        parsedCommand.append(argument)
        parsedCommand.append(message)
        parsedCommand.append(details)
        return parsedCommand

    def setSuspended(self , curThread ):
        """ just suspend provided curThread """
        self._suspended.setSuspended(curThread)


    def resume(self , curThread ):
        """ resume previously suspended thread """
        self._suspended.resume(curThread)


    def buildEvalArguments( self , arg ):
        posEqual = arg.find('=')
        if posEqual == -1:
            return None,None # Syntax error on provided expession couple
        return arg[:posEqual].strip() , arg[posEqual+1:].strip()

    #
    # parse & execute buffer command
    #
    def dealWithCmd( self ,
                     verb ,
                     arg ,
                     myGlobals = globals() ,
                     myLocals = locals()
                   ):
        #cmd = COMMAND
        msgOK = OK
        cmdType = "single"
        silent , silentarg = self.commandSyntax( arg )
        if silent == 'silent':
            arg = silentarg # consume
            # "exec" is the magic way which makes
            # used debuggees dictionaries updatable while
            # stopped in debugging hooks
            cmdType = "exec"
            msgOK = silent
        # we use ';' as a python multiline syntaxic separator
        arg = string.replace(arg,';','\n')
        # execute requested dynamic command on this side
        try:
            # redirect screen and keyboard io to jpydaemon
            oldstd = sys.stdout
            oldstdin = sys.stdin
            sys.stdout=self
            sys.stdin =self
            self._inCommand = 1
            code = compile( arg ,"<string>" , cmdType)
            exec code in myGlobals , myLocals
            sys.stdout=oldstd
            sys.stdin =oldstdin
            self._inCommand = 0
            return _utils.parsedReturned( argument = arg , message = msgOK )
        except:
            try:
                return _utils.populateCMDException(arg,oldstd)
            except:
                tb , exctype , value = sys.exc_info()
                excTrace = traceback.format_exception( tb , exctype , value )
                print excTrace

    #
    # build an xml CDATA structure
    # usage of plus is for jpysource.py xml CDATA encapsulation of itself
    #
    def CDATAForXml( self , data ):
        if sys.platform == 'mvs' :
            return '<'+'!'+ CP037_OPENBRACKET + 'CDATA' + \
               CP037_OPENBRACKET + data + \
               CP037_CLOSEBRACKET+ CP037_CLOSEBRACKET+'>'
        else:
            return '<'+'![CDATA['+ data + ']'+']>'

    #
    # parse & execute buffer command
    #
    def dealWithRead( self , verb , arg ):
        #cmd = READSRC
        # check python code and send back any found syntax error
        if arg == None:
            return _utils.parsedReturned( message = "JPyDaemon ReadSrc Argument missing")
        try:
            arg , lineno = _utils.nextArg(arg)
            candidate = open(arg) # use 2.1 compatible open builtin for Jython
            myBuffer = _utils.parsedReturned( argument = arg , message=OK )
          #
          # append the python source in <FILEREAD> TAG
            myBuffer.append( ['<FILEREAD' ,
                              'fn="'+arg+'"' ,
                              'lineno="'+__builtin__.str(lineno)+'">' +
                              self.CDATAForXml(self._mvsCp037Check(candidate.read())) +
                              '</FILEREAD>' ] )
            return myBuffer
        except IOError, e:
            return _utils.parsedReturned( argument = arg , message = e.strerror )
    #
    # parse & execute buffer command
    #
    def dealWithSetArgs( self , arg ):
        #cmd = SETARGS
        # populate given command line argument before debugging start
        # first slot reserved for program name
        self.debuggeeArgs = [''] # nor args provided
        if arg != None:
            # loop on nextArg
            current , remainder = _utils.nextArg(arg)
            while current != None :
                self.debuggeeArgs.append(current)
                current , remainder = _utils.nextArg(remainder)

          # self.debuggeeArgs = string.split(arg)
        sys.argv = self.debuggeeArgs # store new argument list ins sys argv
        return _utils.parsedReturned( argument = arg , message = OK )

    def isDebuggingOver( self ):
        tList = self.listThreads()
        if ( len(tList) == 0 ) :
            return True
        return False

    def runIt(self, filename , verb ):
        # Start with fresh empty copy of globals and locals and tell the script
        # that it's being run as __main__ to avoid scripts being able to access
        # the tpdb.py namespace.
        mainpyfile = self.canonic(filename)
        self._verb = verb
        globals_ = {"__name__"     : "__main__",
                    "__file__"     : mainpyfile,
                    "__builtin__" : __builtin__ ,
                    }
        locals_ = globals_
        statement = 'execfile( "%s")' % filename
        self.run(statement, myglobals=globals_, mylocals=locals_)

    # load the candidate source to debug
    # Run under debugger control
    def dealWithDebug( self , verb , arg ):
        self.cmd = DEBUG
        if self.debuggee == None:
            result = "source not found : " + arg
            for dirname in sys.path:
                fullname = os.path.join(dirname,arg)
                if os.path.exists(fullname):
                    # Insert script directory in front of module search path
                    # and make it current path (#sourceforge REQID 88108 fix)
                    debugPath = os.path.dirname(fullname)
                    sys.path.insert(0, debugPath)
                    if (  len(debugPath) != 0 ):
                        # following test added for JYTHON support
                        if ( not _utils.isJython ):
                            # chdir not available in jython
                            os.chdir(debugPath)
                    sys.stdout=self.dbgstdout
                    sys.stdin=self.dbgstdin
                    self.debuggee = fullname
                    sys.argv[0] = fullname # keep sys.argv in sync
                    # apply DEBUG rule to threading as well
                    # NB : jython does not implement this function so test
                    #if threading.__dict__.has_key("settrace"):
                    #    threading.settrace(self.trace_dispatch)
                    self.runIt( fullname , verb )
                else :
                    print "inexisting debugee's file : " , fullname
                    return None
                break

    def formatStackElement( self , element ):
        curCode = element[0].f_code
        fName = curCode.co_filename
        line  =  element[1]
        if ( fName == '<string>' ):
            return ("program entry point")
        return _utils.removeForXml(fName + ' (' + __builtin__.str(line) + ') ')

    # populate current stack info to client side
    def dealWithStack( self , frame ):
        stackList , size = self.get_stack ( frame , None )
        stackList.reverse()
        xmlStack = ['<STACKLIST>' ]
        for stackElement in stackList:
            xmlStack.append('<STACK')
            xmlStack.append('content="'+ self.formatStackElement(stackElement) +'"')
            xmlStack.append( '/>')
        xmlStack.append('</STACKLIST>')
        self._connection.populateXmlToClient( xmlStack )

    # populate current threads infos to client side
    def dealWithThread( self  ):
        threadList = threading.enumerate()
        curThread = threading.currentThread()
        xmlThread = ['<THREADLIST>' ]
        for threadElement in threadList:
            if curThread == threadElement :
                current = "true"
            else:
                current = "false"
            state = 'UNDEFINED'
            if hasattr(curThread , 'additionalInfo') :
               # check for living frames for given Thread
               curState = curThread.additionalInfo._state
               if curState == STATE_RUNNING :
                   state = 'RUNNING'
               if curState == STATE_SUSPENDED :
                   state = 'SUSPENDED'
            xmlThread.append('<THREAD')
            xmlThread.append( 'name="'+
                              threadElement.getName()+
                              '" current="'+current+
                              '" state="'+state+'"'
                            )
            xmlThread.append( '/>')
        xmlThread.append('</THREADLIST>')
        self._connection.populateXmlToClient( xmlThread )

    # populate requested disctionary to client side
    def dealWithVariables( self , frame , type , stackIndex  ):
        # get the stack frame first
        stackList , size = self.get_stack ( frame , None )
        stackList.reverse()
        stackElement = stackList[int(stackIndex)]
        if ( type == 'GLOBALS' ):
            variables = stackElement[0].f_globals
        else:
            variables = stackElement[0].f_locals
        xmlVariables = ['<VARIABLES type="'+type+'">' ]
        for mapElement in variables.items():
            mapElmValue = mapElement[1]
            if isinstance(mapElmValue, unicode):
                mapElmValue = mapElmValue.encode(sys.stdout.encoding)
            xmlVariables.append('<VARIABLE ')
            xmlVariables.append('name="'+ _utils.removeForXml(mapElement[0])+'" ')
            xmlVariables.append('content="'+ _utils.removeForXml(__builtin__.str(mapElmValue))+'" ')
            xmlVariables.append('vartype="'+ self.getVarType(mapElement[1])+'" ')
            xmlVariables.append( '/>')
        xmlVariables.append('</VARIABLES>')
        self._connection.populateXmlToClient( xmlVariables )

    # return true when selected element is composite candidate
    def isComposite( self , value ):
        if isinstance(value , types.DictType ) :
            return 0
        elif isinstance(value , types.ListType ) :
            return 0
        elif isinstance(value , types.TupleType ) :
            return 0
        elif not ( isinstance(value , types.StringType ) or \
               isinstance(value , types.ComplexType ) or \
               isinstance(value , types.FloatType ) or \
               isinstance(value , types.IntType ) or \
               isinstance(value , types.LongType ) or \
               isinstance(value , types.NoneType ) or \
               isinstance(value , types.UnicodeType ) ):
            return 1
        else:
            return 0

    # return true when selected element is composite candidate
    def getSimpleType( self , value ):
        if  isinstance( value , types.StringType ):
            return 'String'
        elif isinstance( value , types.ComplexType ):
            return 'ComplexNumber'
        elif isinstance( value , types.FloatType ):
            return 'Float'
        elif isinstance( value , types.IntType ):
            return 'Integer'
        elif isinstance( value , types.LongType ):
            return 'Long'
        elif isinstance(value , types.NoneType ):
            return 'None'
        elif isinstance( value , types.UnicodeType ) :
            return 'Unicode'
        else:
            return 'UNMANAGED DATA TYPE'

    # return true when selected element is map
    def isMap ( self , value ) :
        if isinstance(value , types.DictType ) :
            return 1
        return 0

    # return true when selected element is List
    def isList ( self , value ) :
        if isinstance(value , types.ListType ) :
            return 1
        return 0

    # return true when selected element is List
    def isTuple ( self , value ) :
        if isinstance(value , types.TupleType ) :
            return 1
        return 0

    def getConnection(self) :
        return self._connection

    # return true when selected element is composite candidate
    def getVarType( self , value ):
        if self.isComposite(value):
            return 'COMPOSITE'
        else:
            if self.isMap( value):
                return 'MAP'
            elif self.isList( value):
                return 'LIST'
            elif self.isTuple( value):
                return 'TUPLE'
            return self.getSimpleType(value)

    def populateVariable( self , xmlVariables , name , value ) :
        xmlVariables.append('<VARIABLE ')
        xmlVariables.append('name="'+ name +'" ')
        xmlVariables.append('content="'+ _utils.removeForXml(__builtin__.str(value)) +'" ')
        xmlVariables.append('vartype="'+ self.getVarType(value)+'" ')
        xmlVariables.append( '/>')


    # populate a variable XML structure back
    def dealsWithComposites( self , oName , myGlobals , myLocals ):
        xmlVariables = ['<VARIABLES>' ]
        try :
            myObject = eval(oName ,  myGlobals , myLocals )
            if self.isList(myObject) or self.isTuple(myObject) :
                self.dealsWithLists( xmlVariables , myObject )
            elif self.isMap(myObject) :
                self.dealsWithMaps( xmlVariables , myObject )
            else:
                # standard composite cases
                for key in dir(myObject):
                    try :
                        value = getattr(myObject, key)
                        #if self.isComposite(value):
                        self.populateVariable( xmlVariables , _utils.removeForXml(key) , value )
                    except :
                        # since many kind of exception may arise here specially in jython
                        # just use a general exception case which will prevent
                        # components in exception to get displayed
                        pass
        except NameError :
            self.populateVariable( xmlVariables , _utils.removeForXml(oName) , "NameError : can't guess" )
        xmlVariables.append('</VARIABLES>')
        self._connection.populateXmlToClient( xmlVariables )

    # populate a variable XML structure back Python List case
    def dealsWithLists( self , xmlVariables , myList ):
        for ii in range( len(myList) ) :
            value = myList[ii]
            #if self.isComposite(value):
            self.populateVariable( xmlVariables , '%03i'%(ii) , value )

    # populate a variable XML structure back Python MAP case
    def dealsWithMaps( self , xmlVariables , myMap  ):
        keys = myMap.keys()
        for key in keys :
            value = myMap[key]
            #if self.isComposite(value):
            self.populateVariable( xmlVariables , str(key) , value )

    def variablesSubCommand( self , frame , verb , arg , cmd ):
        self.cmd = cmd
        if ( arg == None ):
            arg = "0"
        else:
            arg , optarg = _utils.nextArg(arg) # split BP arguments
        self.dealWithVariables( frame , verb , arg )
        self.cmd = FREEZE


    # rough command/subcommand syntax analyzer
    def commandSyntax( self , command ):
        self.cmd  = UNKNOWN
        verb , arg  = _utils.nextArg(command)
        return verb , arg


    def quiting( self ):
        sys.stdout=self.stdout
        sys.stdin=self.stdin
        self.debuggee = None
        self.set_quit()

    def setTrace(self , method ) :
        """ Set Debugging Traces """
        try :
            threading.settrace(method)
        except :
            # Jython 2.2 sys set traces globally threading
            # has no settrace methods
            sys.settrace(method)
         # debug traces are in places


    def stopRunningThreads(self):
        threads = threading.enumerate()
        for thread in threads :
            if thread.isAlive() :
                if ( thread.getName() != 'DbgCommanderThread' ) :
                    pass

    def parseSingleCommand( self , command ):
        verb , arg = self.commandSyntax( command )
        if ( string.upper(verb) == "READSRC" ):
            return self.dealWithRead( verb , arg )
        if ( string.upper(verb) == "SETARGS" ):
            return self.dealWithSetArgs( arg )
        elif ( string.upper(verb) == "DBG" ):
            return self.dealWithDebug( verb, arg )
        elif ( string.upper(verb) == "STOP"):
            self.inProgress = False # Stop GlobalCommanderThread
            raise dbgutils.JpyDbgQuit
        else:
            return _utils.parsedReturned( message = "JPyDaemon SYNTAX ERROR : " + command )

    # receive a command when in debugging state using debuggee's frame local and global
    # contexts
    def parseSubCommand( self , command , frame , lthread ):
        if ( command == None ): # in case of IP socket Failures
            return UNKNOWN
        verb , arg = self.commandSyntax( command )
        if ( string.upper(verb) == "CMD" ):
            self.populateCommandToClient( command ,
                                        self.dealWithCmd( verb ,
                                                          arg ,
                                                          myGlobals= frame.f_globals ,
                                                          myLocals = frame.f_locals
                                                        )
                                        )
            self.cmd = FREEZE

        elif ( string.upper(verb) == "READSRC" ):
            self.populateCommandToClient( command ,
                                        self.dealWithRead( verb , arg )
                                      )
            self.cmd = FREEZE

        elif ( string.upper(verb) == "NEXT" ):
            self.cmd = NEXT
            lthread.additionalInfo.cmd=NEXT
            # Store the backframe to check that next statement matches it
            lthread.additionalInfo.stop_frame=lthread.additionalInfo.last_line_frame.f_back
            if lthread.additionalInfo.stop_frame != None :
                _DEBUG( 'NEXT stop in frame=%s' %( str(lthread.additionalInfo.stop_frame.f_lineno) )  )

            self.set_next(frame)
        elif ( string.upper(verb) == "STEP" ):
            self.cmd = STEP
            lthread.additionalInfo.cmd=STEP
            # self.set_step()
        elif ( string.upper(verb) == "STEPOUT" ):
            self.cmd = STEP_RETURN
            lthread.additionalInfo.cmd=STEP_RETURN
            # Store the backframe to check that next statement matches it
            lthread.additionalInfo.step_out=lthread.additionalInfo.last_line_frame.f_back
        elif ( string.upper(verb) == "RUN" ):
            self.dbgContinue = True
            self.set_continue()
        elif ( string.upper(verb) == "BP+"):
            self.cmd = SET_BP
            # split the command line argument on the last blank
            _DEBUG( 'BP+=%s' %(arg))
            file , optarg = _utils.nextArg(arg)
            line ,optarg= _utils.nextArg(optarg)
            temp , optarg = _utils.nextArg(optarg)
            if temp != None :
                temp = int(temp)
            condition , optarg = _utils.nextArg(optarg)
            hits , optarg = _utils.nextArg(optarg)
            hitsStyle , optarg = _utils.nextArg(optarg)
            if hits != None :
                hits = int(hits)
                if  ( hitsStyle == "GREATER") :
                    hitsStyle = HIT_GREATER_THAN
                elif  ( hitsStyle == "MULTIPLE") :
                    hitsStyle = HIT_MULTIPLE_OF
                else :
                    hitsStyle = HIT_EQUALS_TO
            else :
                hits = 0
            _DEBUG( 'hist=%s' %(str(hits)))
            self.set_break( file , int(line) , temp , condition , hits , hitsStyle)
            self.cmd = FREEZE
        elif ( string.upper(verb) == "STACK"):
            self.cmd = STACK
            self.dealWithStack(frame)
            self.cmd = FREEZE
        elif ( string.upper(verb) == "THREAD"):
            self.cmd = THREAD
            self.dealWithThread()
            self.cmd = FREEZE
        elif ( string.upper(verb) == "LOCALS"):
            self.variablesSubCommand( frame , verb , arg , LOCALS )
        elif ( string.upper(verb) == "GLOBALS"):
            self.variablesSubCommand( frame , verb , arg , GLOBALS )
        elif ( string.upper(verb) == "COMPOSITE"):
            self.cmd=COMPOSITE
            arg , optarg = _utils.nextArg(arg) # split BP arguments
            self.dealsWithComposites( arg ,  frame.f_globals ,  frame.f_locals )
            self.cmd = FREEZE
        elif ( string.upper(verb) == "BP-"):
            self.cmd = CLEAR_BP
            file , optarg = _utils.nextArg(arg) # split BP arguments
            line , optarg = _utils.nextArg(optarg) # and get line number
            self.clear_break( file, int(line) )	# must include lin number!
            self.cmd = FREEZE
        elif ( string.upper(verb) == "KILL"):
            self.cmd = QUIT
            # raise JpyDbgQuit exception to force termination on  debug suspended thread
            raise dbgutils.JpyDbgQuit
        elif ( string.upper(verb) == "GLBCMD"):
            # to avoid Jython deadlocks the global commands are executed under the control
            # of BREAK thread
            self.parseCommand(arg)
            self.cmd = FREEZE # A DO NOTHING ON GLOBAL JUST TO UNBLOCK SUSPENDED THREAD
        return self.cmd

    def terminateDaemon( self  ):
        """ terminate debugger IP session """
        self._connection.terminate()
        print "'+++ JPy/sessionended/"
        sys.stdout = self.stdout
        sys.stdin = self.stdin
        print "deamon ended\n"
        sys.exit()


    # send command result back
    def populateCommandToClient( self , command , result ):
        self._connection.populateCommandToClient(command,result)

    # check and execute a received command
    def parseCommand( self , command ):
        # IP exception populating None object
        result  = self.parseSingleCommand(command)
        if self.startInProgress and result  :
            self.populateCommandToClient( command , result )


    # start the deamon
    def start( self , port = PORT , host = None , debuggee = None ,debuggeeArgs = None ):
        #if not self.connect(host,port) :
        #    return # just leave
        # define and enter the Networking Thread
        self._connection = dbgnetwork.NetworkDebuggingSession(host,port)
        if self._connection.connect() :
            _DEBUG('connection is up ')
            # If connection is ready start the network thread
            # self._connection.start()
            # Send A Welcome ACK back
            welcome = [ '<WELCOME/>' ]
            # populate debuggee's name for remote debugging bootstrap
            if debuggee != None:
                welcome = [ '<WELCOME' ,
                            'debuggee="'+ _utils.removeForXml(debuggee)]
                if debuggeeArgs != None:
                    welcome.append(string.join(debuggeeArgs))
                  # populate arguments after program Name
                # finally append XML closure
                welcome.append('" />')
            self._connection.populateXmlToClient( welcome )
             # next wait for first command populated by commander
            command = self._connection.getNextDebuggerCommand()
            self.startInProgress = True
            while self.startInProgress  and command != None :
                gblcmd , command = _utils.nextArg(command)
                self.parseCommand( command )
                if self.startInProgress :
                    _DEBUG('Waiting for Global commands ....' )
                    command = self._connection.getNextDebuggerCommand()
                    _DEBUG('command %s : PROCESSED' % (command))
            _DEBUG('*** Starter Thread IS TERMINATED')

#
# Instanciate a client side debugging session
#
def remoteDbgSession( localDebuggee , host , port=PORT , args = None ):
    minstance = JPyDbg()
    minstance.start( host=host ,
                     port=port ,
                     debuggee=localDebuggee ,
                     debuggeeArgs=args
                   )

# start a listening instance when invoked as main program
# without arguments
# when [host [port]] are provided as argv jpydamon will try to
# connect back host port instead of listening
if __name__ == "__main__":
    # use mainthread for user app
    _DEBUG("entering debugging starter")
    mainthread = threading.currentThread()
    mainthread.name = STARTER_THREAD_NAME
    instance = JPyDbg()
    print "args = " , sys.argv
    host = _utils.consumeArgv()
    port = _utils.consumeArgv()
    if port == None:
        port = PORT
    else:
        port = int(port)
    # Jython check and support
    if ( os.name == "java" ):
        pathArgs = "JYTHONPATH"
        os.environ[pathArgs] = os.environ["PYTHONPATH"]
    else:
        pathArgs = "PYTHONPATH"
    pythonPath = dbgutils.PythonPathHandler(None)
    pythonPath.getPyPathFromEnv()

    # finally get the optional local debuggee
    localDebuggee = _utils.consumeArgv()
    print "localDebuggee=" , localDebuggee
    #
    instance.start( host=host ,
                    port=port ,
                    debuggee=localDebuggee ,
                    debuggeeArgs=sys.argv
                  )
    # strater(main) thread termination
    _DEBUG("quiting debugging starter")

