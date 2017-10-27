class Facing:
    def __init__(self, id, short, name):
        self.id = id
        self.name = name
        self.short = short

    def __repr__(self):
        return self.name.capitalize()
            
    def __str__(self):
        return self.name.capitalize()

def fromId(id):
    if(id == NORTH.id):
        return NORTH
    if(id == SOUTH.id):
        return SOUTH
    if(id == WEST.id):
        return WEST
    if(id == EAST.id):
        return EAST
    if(id == UP.id):
        return UP
    if(id == DOWN.id):
        return DOWN
    raise ValueError(str(id) + " is not a valid facing Id")
    
def fromName(name):
    name = name.lower()
    if(name == NORTH.name):
        return NORTH
    if(name == SOUTH.name):
        return SOUTH
    if(name == WEST.name):
        return WEST
    if(name == EAST.name):
        return EAST
    if(name == UP.name):
        return UP
    if(name == DOWN.name):
        return DOWN
    raise ValueError(name + " is not a valid facing value")
    
def fromLetter(short):
    short = short.lower() 
    if(short == NORTH.short):
        return NORTH
    if(short == SOUTH.short):
        return SOUTH
    if(short == WEST.short):
        return WEST
    if(short == EAST.short):
        return EAST
    if(short == UP.short):
        return UP
    if(short == DOWN.short):
        return DOWN
    raise ValueError(short + " is not a valid facing value")

NORTH = Facing(2, "n", "north")
EAST = Facing(5, "e", "east")
SOUTH = Facing(3, "s", "south")
WEST = Facing(4, "w", "west")
UP = Facing(1, "u", "up")
DOWN = Facing(0, "d", "down")