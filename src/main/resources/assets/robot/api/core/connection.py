from __future__ import absolute_import

import socket
import select
import sys
import os
import platform
import base64
from hashlib import md5
from .util import flatten_parameters_to_string

""" @author: Aron Nieminen, Mojang AB"""

class RequestError(Exception):
    def __init__(self, message, *errors):

        # Call the base class constructor with the parameters it needs
        super(RequestError, self).__init__(message)

        # Now for your custom code...
        self.errors = errors

class Connection:
    """Connection to a Minecraft Pi game"""
    RequestFailed = "FAIL|"

    def __init__(self, address=None, port=None):
        self.windows = (platform.system() == "Windows" or platform.system().startswith("CYGWIN_NT"))
        if address==None:
            try:
                 address = os.environ['MINECRAFT_API_HOST']
            except KeyError:
                 address = "localhost"
        if port==None:
            try:
                 port = int(os.environ['MINECRAFT_API_PORT'])
            except KeyError:
                 port = 4711
        if sys.version_info[0] >= 3:
            self.send = self.send_python3
            self.send_flat = self.send_flat_python3
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((address, port))
        self.readFile = self.socket.makefile("r")
        self.lastSent = ""
            

    def __del__(self):
        if self.windows:
            self.close()

    def close(self, id=None):
        try:
            if(id == None):
                self.sendReceive("mcpi.close")
            else:
                self.sendReceive("mcpi.close", id)
        except:
            pass
        try:
            self.socket.close()
        except:
            pass
            
    @staticmethod
    def tohex(data):
        return "".join((hex(b) for b in data))

    def drain(self):
        """Drains the socket of incoming data"""
        while True:
            readable, _, _ = select.select([self.socket], [], [], 0.0)
            if not readable:
                break
            data = self.socket.recv(1500)
            if not data:
                self.socket.close()
                raise ValueError('Socket got closed')
            e =  "Drained Data: <%s>\n"%data.strip()
            e += "Last Message: <%s>\n"%self.lastSent.strip()
            sys.stderr.write(e)
                                             
    def send(self, f, *data):
        """Sends data. Note that a trailing newline '\n' is added here"""
        s = "%s(%s)\n"%(f, flatten_parameters_to_string(data))
        #print "s:"+s+":"
        self.drain()
        self.lastSent = s
        self.socket.sendall(s)

    def send_python3(self, f, *data):
        """Sends data. Note that a trailing newline '\n' is added here"""
        s = "%s(%s)\n"%(f, flatten_parameters_to_string(data))
        #print "f,data:",f,data
        self.drain()
        self.lastSent = s
        self.socket.sendall(s.encode("utf-8"))

    def send_flat(self, f, data):
        """Sends data. Note that a trailing newline '\n' is added here"""
#        print "f,data:",f,list(data)
        s = "%s(%s)\n"%(f, ",".join(data))
        self.drain()
        self.lastSent = s
        self.socket.sendall(s)

    def send_flat_python3(self, f, data):
        """Sends data. Note that a trailing newline '\n' is added here"""
#        print "f,data:",f,list(data)
        s = "%s(%s)\n"%(f, ",".join(data))
        self.drain()
        self.lastSent = s
        self.socket.sendall(s.encode("utf-8"))

    def receive(self):
        """Receives data. Note that the trailing newline '\n' is trimmed"""
        s = self.readFile.readline().rstrip("\n")
        if Connection.RequestFailed in s:
            raise RequestError(s[5:])
        return s

    def sendReceive(self, *data):
        """Sends and receive data"""
        self.send(*data)
        return self.receive()

    def sendReceive_flat(self, f, data):
        """Sends and receive data"""
        self.send_flat(f, data)
        return self.receive()
