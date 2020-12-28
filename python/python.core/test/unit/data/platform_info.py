#! /usr/bin/python

import sys
import os
import platform

command = sys.executable
version = platform.python_version()
if platform.system() == "Java":
  print "platform.name="+ "Jython " + version
else:
  print "platform.name="+ "Python " + version
print "python.command="+ command
path = ""
for pathItem in sys.path:
  path += pathItem + os.pathsep
print "python.path="+path

if platform.system()== "Java":  
  from java.lang import System
  classpath = System.getProperty('java.class.path')
  print classpath

