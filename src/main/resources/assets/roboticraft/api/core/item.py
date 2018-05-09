from __future__ import absolute_import
import re


class Item:
    """Minecraft Items"""
    
    def __init__(self, id, data=0, name='', nbt=None):
        self.id = id
        self.data = data
        self.name = name
        if nbt is not None and len(nbt) == 0:
            self.nbt = None
        else:
            self.nbt = nbt

    def __eq__(self, rhs):
        try:
            return self.id == rhs.id and self.data == rhs.data and self.nbt == rhs.nbt
        except:
            return self.data == 0 and self.nbt is None and self.id == rhs

    def __ne__(self, rhs):
        return not self.__eq__(rhs)

    def __hash__(self):
        h = (self.id << 8) + self.data
        if self.nbt is not None:
            h ^= hash(self.nbt)

    def withData(self, data):
        return Item(self.id, data)
                
    def getName(self):
        return self.name

    def __iter__(self):
        """Allows a Item to be sent whenever id [and data] is needed"""
        if self.nbt is not None:
           return iter((self.id, self.data, self.nbt))
        else:
           return iter((self.id, self.data))
           
    def __hash__(self):
        return hash((self.id, self.data, self.nbt))

    def __repr__(self):
        if self.nbt is None:
            return self.getName()  # + " Item(%d, %d)"%(self.id, self.data)
        else:
            return self.getName()  # + " Item(%d, %d, %s)"%(self.id, self.data, repr(self.nbt))

    def __str__(self):
        return self.getName()
        
