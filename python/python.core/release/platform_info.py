#! /usr/bin/python

import sys
import os

command = sys.executable
major = sys.version_info[0]
minor = sys.version_info[1]
micro = sys.version_info[2]
sourceLevel = str(major) + '.' + str(minor)
version = sourceLevel + '.' + str(micro)
isJava = sys.platform.count("java")
if isJava :
    print("platform.name="+ "Jython " + version)
else:
    print("platform.name="+ "Python " + version)
print("platform.sourcelevel=" + sourceLevel)
if command != None :
    print("python.command="+ command.replace("\\", "\\\\"))
path = ""
for pathItem in sys.path:
    path += pathItem + os.pathsep
print("python.path="+path.replace("\\", "\\\\"))

if isJava  :
    from java.lang import System
    classpath = System.getProperty('java.class.path')
    print(classpath)

