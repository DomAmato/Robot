from __future__ import absolute_import
import re

class Block:
    """Minecraft PI block description. Can be sent to Minecraft.setBlock/s"""
    
    MATERIAL_DEFAULT = 0
    MATERIAL_WOOD = 1
    MATERIAL_STONE = 2
    MATERIAL_PLASTIC = 3
    MATERIAL_EMISSIVE = 4
    MATERIAL_ROUGH = 5
    
    MAX_MATERIAL = MATERIAL_ROUGH
    
    def __init__(self, id, data=0, nbt=None):
        self.id = id
        self.data = data
        if nbt is not None and len(nbt)==0:
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
        return Block(self.id, data)
        
    def getRGBA(self):
        try:
            return Block.toRGBA[self][0:4]
        except:
            try:
                return Block.toRGBA[Block(self.id)][0:4]
            except:
                return Block.toRGBA[Block(1)][0:4]
                
    def getName(self):
        try:
            return Block.idToBlockName[self]
        except:
            try:
                return Block.idToBlockName[Block(self.id)]
            except:
                return Block.idToBlockName[Block(1)]
                
    def getMaterial(self):
        entry = None
        try:
            entry = Block.toRGBA[self]
        except:
            try:
                entry = Block.toRGBA[Block(self.id)]
            except:
                return Block.MATERIAL_DEFAULT
        if len(entry) <= 4:
            return Block.MATERIAL_DEFAULT
        else:
            return entry[4]

    def __iter__(self):
        """Allows a Block to be sent whenever id [and data] is needed"""
        if self.nbt is not None:
           return iter((self.id, self.data, self.nbt))
        else:
           return iter((self.id, self.data))
           
    def __hash__(self):
        return hash((self.id, self.data, self.nbt))

    def __repr__(self):
        if self.nbt is None:
            return self.getName() #+ " Block(%d, %d)"%(self.id, self.data)
        else:
            return self.getName() #+ " Block(%d, %d, %s)"%(self.id, self.data, repr(self.nbt))

    def __str__(self):
        return self.getName()
    
    @staticmethod
    def byName(name, default=None):
        components = re.split('[\s,:]+', name.strip().replace(".id", "").upper(), maxsplit=3)
        newBlock = Block(None,data=0,nbt=None)
        if components[0][0].isalpha():
            try:
                b = globals()[components[0]]
                if isinstance(b, Block):
                    newBlock.id = b.id
                    newBlock.data = b.data
            except:
                if default is None:
                    newBlock.id = STONE.id
                    newBlock.data = STONE.data
                else:
                    newBlock.id = default.id
                    newBlock.data = default.data
        if newBlock.id is None:
            newBlock.id = int(components[0])
        if len(components) > 1 and components[1].isdigit():
            newBlock.data = int(components[1])
        if components[-1][0] == '{':
            newBlock.nbt = components[-1]
        return newBlock
        
AIR                 = Block(0)
STONE               = Block(1)
GRASS               = Block(2)
DIRT                = Block(3)
COBBLESTONE         = Block(4)
WOOD_PLANKS         = Block(5)
SAPLING             = Block(6)
BEDROCK             = Block(7)
WATER_FLOWING       = Block(8)
WATER               = WATER_FLOWING
WATER_STATIONARY    = Block(9)
LAVA_FLOWING        = Block(10)
LAVA                = LAVA_FLOWING
LAVA_STATIONARY     = Block(11)
SAND                = Block(12)
GRAVEL              = Block(13)
GOLD_ORE            = Block(14)
IRON_ORE            = Block(15)
COAL_ORE            = Block(16)
WOOD                = Block(17)
LEAVES              = Block(18)
GLASS               = Block(20)
LAPIS_LAZULI_ORE    = Block(21)
LAPIS_LAZULI_BLOCK  = Block(22)
SANDSTONE           = Block(24)
SANDSTONE_SMOOTH    = Block(SANDSTONE.id, 2)
SANDSTONE_CHISELED  = Block(SANDSTONE.id, 1)
BED                 = Block(26)
BED_BLOCK           = Block(26)
COBWEB              = Block(30)
GRASS_TALL          = Block(31)
WOOL                = Block(35)
FLOWER_YELLOW       = Block(37)
FLOWER_CYAN         = Block(38)
MUSHROOM_BROWN      = Block(39)
MUSHROOM_RED        = Block(40)
GOLD_BLOCK          = Block(41)
IRON_BLOCK          = Block(42)
STONE_SLAB_DOUBLE   = Block(43)
STONE_SLAB          = Block(44)
BRICK_BLOCK         = Block(45)
TNT                 = Block(46)
BOOKSHELF           = Block(47)
MOSS_STONE          = Block(48)
OBSIDIAN            = Block(49)
TORCH               = Block(50)
FIRE                = Block(51)
STAIRS_WOOD         = Block(53)
CHEST               = Block(54)
DIAMOND_ORE         = Block(56)
DIAMOND_BLOCK       = Block(57)
CRAFTING_TABLE      = Block(58)
FARMLAND            = Block(60)
FURNACE_INACTIVE    = Block(61)
FURNACE_ACTIVE      = Block(62)
DOOR_WOOD           = Block(64)
LADDER              = Block(65)
STAIRS_COBBLESTONE  = Block(67)
DOOR_IRON           = Block(71)
REDSTONE_ORE        = Block(73)
STONE_BUTTON        = Block(77)
SNOW                = Block(78)
ICE                 = Block(79)
SNOW_BLOCK          = Block(80)
CACTUS              = Block(81)
CLAY                = Block(82)
SUGAR_CANE          = Block(83)
FENCE               = Block(85)
GLOWSTONE_BLOCK     = Block(89)
BEDROCK_INVISIBLE   = Block(95)
STAINED_GLASS       = Block(95)
STONE_BRICK         = Block(98)
GLASS_PANE          = Block(102)
MELON               = Block(103)
FENCE_GATE          = Block(107)
WOOD_BUTTON         = Block(143)
REDSTONE_BLOCK      = Block(152)
QUARTZ_BLOCK        = Block(155)
HARDENED_CLAY_STAINED = Block(159)
SEA_LANTERN         = Block(169)
CARPET              = Block(171)
COAL_BLOCK          = Block(173)
GLOWING_OBSIDIAN    = SEA_LANTERN
NETHER_REACTOR_CORE = SEA_LANTERN
REDSTONE_LAMP_INACTIVE = Block(123)
REDSTONE_LAMP_ACTIVE   = Block(124)

SUNFLOWER  = Block(175,0)
LILAC      = Block(175,1)
DOUBLE_TALLGRASS = Block(175,2)
LARGE_FERN       = Block(175,3)
ROSE_BUSH        = Block(175,4)
PEONY            = Block(175,5)

WOOL_WHITE = Block(WOOL.id, 0)
WOOL_ORANGE = Block(WOOL.id, 1)
WOOL_MAGENTA = Block(WOOL.id, 2)
WOOL_LIGHT_BLUE = Block(WOOL.id, 3)
WOOL_YELLOW = Block(WOOL.id, 4)
WOOL_LIME = Block(WOOL.id, 5)
WOOL_PINK = Block(WOOL.id, 6)
WOOL_GRAY = Block(WOOL.id, 7)
WOOL_LIGHT_GRAY = Block(WOOL.id, 8)
WOOL_CYAN = Block(WOOL.id, 9)
WOOL_PURPLE = Block(WOOL.id, 10)
WOOL_BLUE = Block(WOOL.id, 11)
WOOL_BROWN = Block(WOOL.id, 12)
WOOL_GREEN = Block(WOOL.id, 13)
WOOL_RED = Block(WOOL.id, 14)
WOOL_BLACK = Block(WOOL.id, 15)

CARPET_WHITE = Block(CARPET.id, 0)
CARPET_ORANGE = Block(CARPET.id, 1)
CARPET_MAGENTA = Block(CARPET.id, 2)
CARPET_LIGHT_BLUE = Block(CARPET.id, 3)
CARPET_YELLOW = Block(CARPET.id, 4)
CARPET_LIME = Block(CARPET.id, 5)
CARPET_PINK = Block(CARPET.id, 6)
CARPET_GRAY = Block(CARPET.id, 7)
CARPET_LIGHT_GRAY = Block(CARPET.id, 8)
CARPET_CYAN = Block(CARPET.id, 9)
CARPET_PURPLE = Block(CARPET.id, 10)
CARPET_BLUE = Block(CARPET.id, 11)
CARPET_BROWN = Block(CARPET.id, 12)
CARPET_GREEN = Block(CARPET.id, 13)
CARPET_RED = Block(CARPET.id, 14)
CARPET_BLACK = Block(CARPET.id, 15)

STAINED_GLASS_WHITE = Block(STAINED_GLASS.id, 0)
STAINED_GLASS_ORANGE = Block(STAINED_GLASS.id, 1)
STAINED_GLASS_MAGENTA = Block(STAINED_GLASS.id, 2)
STAINED_GLASS_LIGHT_BLUE = Block(STAINED_GLASS.id, 3)
STAINED_GLASS_YELLOW = Block(STAINED_GLASS.id, 4)
STAINED_GLASS_LIME = Block(STAINED_GLASS.id, 5)
STAINED_GLASS_PINK = Block(STAINED_GLASS.id, 6)
STAINED_GLASS_GRAY = Block(STAINED_GLASS.id, 7)
STAINED_GLASS_LIGHT_GRAY = Block(STAINED_GLASS.id, 8)
STAINED_GLASS_CYAN = Block(STAINED_GLASS.id, 9)
STAINED_GLASS_PURPLE = Block(STAINED_GLASS.id, 10)
STAINED_GLASS_BLUE = Block(STAINED_GLASS.id, 11)
STAINED_GLASS_BROWN = Block(STAINED_GLASS.id, 12)
STAINED_GLASS_GREEN = Block(STAINED_GLASS.id, 13)
STAINED_GLASS_RED = Block(STAINED_GLASS.id, 14)
STAINED_GLASS_BLACK = Block(STAINED_GLASS.id, 15)

HARDENED_CLAY_STAINED_WHITE = Block(HARDENED_CLAY_STAINED.id, 0)
HARDENED_CLAY_STAINED_ORANGE = Block(HARDENED_CLAY_STAINED.id, 1)
HARDENED_CLAY_STAINED_MAGENTA = Block(HARDENED_CLAY_STAINED.id, 2)
HARDENED_CLAY_STAINED_LIGHT_BLUE = Block(HARDENED_CLAY_STAINED.id, 3)
HARDENED_CLAY_STAINED_YELLOW = Block(HARDENED_CLAY_STAINED.id, 4)
HARDENED_CLAY_STAINED_LIME = Block(HARDENED_CLAY_STAINED.id, 5)
HARDENED_CLAY_STAINED_PINK = Block(HARDENED_CLAY_STAINED.id, 6)
HARDENED_CLAY_STAINED_GRAY = Block(HARDENED_CLAY_STAINED.id, 7)
HARDENED_CLAY_STAINED_LIGHT_GRAY = Block(HARDENED_CLAY_STAINED.id, 8)
HARDENED_CLAY_STAINED_CYAN = Block(HARDENED_CLAY_STAINED.id, 9)
HARDENED_CLAY_STAINED_PURPLE = Block(HARDENED_CLAY_STAINED.id, 10)
HARDENED_CLAY_STAINED_BLUE = Block(HARDENED_CLAY_STAINED.id, 11)
HARDENED_CLAY_STAINED_BROWN = Block(HARDENED_CLAY_STAINED.id, 12)
HARDENED_CLAY_STAINED_GREEN = Block(HARDENED_CLAY_STAINED.id, 13)
HARDENED_CLAY_STAINED_RED = Block(HARDENED_CLAY_STAINED.id, 14)
HARDENED_CLAY_STAINED_BLACK = Block(HARDENED_CLAY_STAINED.id, 15)

LEAVES_OAK_DECAYABLE = Block(LEAVES.id, 0)
LEAVES_SPRUCE_DECAYABLE = Block(LEAVES.id, 1)
LEAVES_BIRCH_DECAYABLE = Block(LEAVES.id, 2)
LEAVES_JUNGLE_DECAYABLE = Block(LEAVES.id, 3)
LEAVES_OAK_PERMANENT = Block(LEAVES.id, 4)
LEAVES_SPRUCE_PERMANENT = Block(LEAVES.id, 5)
LEAVES_BIRCH_PERMANENT = Block(LEAVES.id, 6)
LEAVES_JUNGLE_PERMANENT = Block(LEAVES.id, 7)
LEAVES_ACACIA_DECAYABLE = LEAVES_OAK_DECAYABLE
LEAVES_DARK_OAK_DECAYABLE = LEAVES_JUNGLE_DECAYABLE
LEAVES_ACACIA_PERMANENT = LEAVES_OAK_PERMANENT
LEAVES_DARK_OAK_PERMANENT = LEAVES_JUNGLE_PERMANENT

# the following may or may not be found in PI and other older ersions
BONE_BLOCK = Block(216)    
DIRT_COARSE = Block(DIRT.id, 1)
DIRT_PODZOL = Block(DIRT.id, 2)
EMERALD_BLOCK = Block(133)
EMERALD_ORE = Block(129)
END_BRICKS = Block(206)
END_STONE = Block(121)
HARDENED_CLAY = Block(172)
ICE_PACKED = Block(174)
NETHERRACK = Block(87)
NETHER_WART_BLOCK = Block(214)
NOTEBLOCK = Block(25)
WOOD_PLANKS_OAK = Block(WOOD_PLANKS.id, 0)
WOOD_PLANKS_SPRUCE = Block(WOOD_PLANKS.id, 1)
WOOD_PLANKS_BIRCH = Block(WOOD_PLANKS.id, 2)
WOOD_PLANKS_JUNGLE = Block(WOOD_PLANKS.id, 3)
WOOD_PLANKS_ACACIA = Block(WOOD_PLANKS.id, 4)
WOOD_PLANKS_DARK_OAK = Block(WOOD_PLANKS.id, 5)
PRISMARINE = Block(168)
PRISMARINE_BRICKS = Block(PRISMARINE.id, 1)
PRISMARINE_DARK = Block(PRISMARINE.id, 2)
PURPUR_BLOCK = Block(201)
PURPUR_PILLAR = Block(202)
RED_NETHER_BRICK = Block(215)
RED_SANDSTONE = Block(179)
RED_SANDSTONE_CHISELED = Block(RED_SANDSTONE.id, 1)
RED_SANDSTONE_SMOOTH = Block(RED_SANDSTONE.id, 2)
SLIME_BLOCK = Block(165)
SOUL_SAND = Block(88)
SPONGE = Block(19)
SPONGE_WET = Block(SPONGE.id, 1)
STONE_GRANITE = Block(STONE.id, 1)
STONE_GRANITE_SMOOTH = Block(STONE.id, 2)
STONE_DIORITE = Block(STONE.id, 3)
STONE_DIORITE_SMOOTH = Block(STONE.id, 4)
STONE_ANDESITE = Block(STONE.id, 5)
STONE_ANDESITE_SMOOTH = Block(STONE.id, 6)
ANVIL = Block(145)
BEACON = Block(138)
BED_OBJECT = Block(355)
BEETROOT = Block(434)
BREWING_STAND = Block(117)
CAKE = Block(92)
CARROTS = Block(141)
CAULDRON = Block(380)
COMMAND_BLOCK = Block(137)
CHORUS_FLOWER = Block(200)
CHORUS_PLANT = Block(199)
COCOA_PLANT = Block(127)
COMPARATOR_OFF = Block(149)
COMPARATOR_ON = Block(150)
DAYLIGHT_SENSOR = Block(151)
DEADBUSH = Block(32)
DISPENSER = Block(23)
DOOR_ACACIA = Block(430)
DOOR_BIRCH = Block(428)
DOOR_DARK_OAK = Block(431)
DOOR_JUNGLE = Block(429)
DOOR_SPRUCE = Block(427)
DRAGON_EGG = Block(122)
DROPPER = Block(158)
ENCHANTING_TABLE = Block(116)
END_PORTAL_FRAME = Block(120)
END_ROD = Block(198)
FERN = Block(GRASS_TALL.id, 2)
FLOWER_ALLIUM = Block(FLOWER_CYAN.id, 2)
FLOWER_BLUE_ORCHID = Block(FLOWER_CYAN.id, 1)
DANDELION = Block(37)
FLOWER_AZURE_BLUET = Block(FLOWER_CYAN.id, 3)
FLOWER_OXEYE_DAISY = Block(FLOWER_CYAN.id, 8)
FLOWER_TULIP_RED = Block(FLOWER_CYAN.id, 4)
FLOWER_TULIP_ORANGE = Block(FLOWER_CYAN.id, 5)
FLOWER_TULIP_WHITE = Block(FLOWER_CYAN.id, 6)
FLOWER_TULIP_PINK = Block(FLOWER_CYAN.id, 7)
FROSTED_ICE = Block(212)
FLOWER_POT = Block(140)
GRASS_PATH = Block(208)
HAY_BLOCK = Block(170)
HOPPER = Block(154)
IRON_BARS = Block(101)
IRON_TRAPDOOR = Block(167)
ITEM_FRAME = Block(389)
JUKEBOX = Block(84)
LEVER = Block(69)
ACACIA_WOOD = Block(162,0)
DARK_OAK_WOOD = Block(162,1)
BIRCH_WOOD = Block(WOOD.id, 2)
SPRUCE_WOOD = Block(WOOD.id, 1)
JUNGLE_WOOD = Block(WOOD.id, 3)
MAGMA = Block(378)
MELON_BLOCK = Block(103)
MOB_SPAWNER = Block(52)
MUSHROOM_BLOCK_BROWN = Block(99)
MUSHROOM_BLOCK_RED = Block(100)
MYCELIUM = Block(110)
NETHER_BRICK = Block(112)
NETHER_WART = Block(115)
PISTON = Block(33)
PISTON_HEAD = Block(34)
PORTAL = Block(90)
POTATOES = Block(142)
PUMPKIN_ACTIVE = Block(91)
PUMPKIN_INACTIVE = Block(86)
QUARTZ_BLOCK_CHISELED = Block(155,1)
NETHER_QUARTZ_ORE = Block(153)
RAIL_ACTIVATOR = Block(157)
RAIL_DETECTOR = Block(28)
RAIL_GOLDEN = Block(27)
RAIL_NORMAL = Block(66)
RED_SAND = Block(SAND.id, 1)
REDSTONE_TORCH_INACTIVE = Block(75)
REDSTONE_TORCH_ACTIVE = Block(76)
REDSTONE_REPEATER_INACTIVE = Block(93)
REDSTONE_REPEATER_ACTIVE = Block(94)
SAPLING_SPRUCE = Block(SAPLING.id, 1)
SAPLING_BIRCH = Block(SAPLING.id, 2)
SAPLING_JUNGLE = Block(SAPLING.id, 3)
SAPLING_ACACIA = Block(SAPLING.id, 4)
SAPLING_DARK_OAK = Block(SAPLING.id, 5)
STONE_BRICK_CHISELED = Block(STONE_BRICK.id, 3)
STONE_BRICK_CRACKED = Block(STONE_BRICK.id, 2)
STONE_BRICK_MOSSY = Block(STONE_BRICK.id, 1)
TRAPDOOR = Block(96)
TRIPWIRE = Block(132)
TRIPWIRE_HOOK = Block(131)
VINE = Block(106)
WATERLILY = Block(111)
WHEAT = Block(59)

Block.idToBlockName = {
      AIR: "Air",
      ANVIL: "Anvil",
      BEACON: "Beacon",
      BED_OBJECT: "Bed",
      BED_BLOCK: "Bed",
      BEDROCK: "Bedrock",
      BEETROOT: "Beetroot",
      BONE_BLOCK: "Bone",
      BOOKSHELF: "Bookshelf",
      BREWING_STAND: "Brewing Stand",
      BRICK_BLOCK: "Brick",
      CACTUS: "Cactus",
      CAKE: "Cake",
      CARPET_BLACK: "Black Carpet",
      CARPET_BLUE: "Blue Carpet",
      CARPET_BROWN: "Brown Carpet",
      CARPET_CYAN: "Cyan Carpet",
      CARPET_GRAY: "Gray Carpet",
      CARPET_GREEN: "Green Carpet",
      CARPET_LIGHT_BLUE: "Light Blue Carpet",
      CARPET_LIGHT_GRAY: "Light Gray Carpet",
      CARPET_LIME: "Lime Carpet",
      CARPET_MAGENTA: "Magenta Carpet",
      CARPET_ORANGE: "Orange Carpet",
      CARPET_PINK: "Pink Carpet",
      CARPET_PURPLE: "Purple Carpet",
      CARPET_RED: "Red Carpet",
      CARPET_WHITE: "White Carpet",
      CARPET_YELLOW: "Yellow Carpet",
      CARROTS: "Carrots",
      CAULDRON: "Cauldron",
      CHEST: "Chest",
      CHORUS_FLOWER: "Chorus Flower",
      CHORUS_PLANT: "Chorus Plant",
      CLAY: "Clay",
      COAL_BLOCK: "Coal Block",
      COAL_ORE: "Coal Ore",
      COCOA_PLANT: "Cocoa",
      COBBLESTONE: "Cobblestone",
      STAIRS_COBBLESTONE: "Cobblestone Stairs",
      COMMAND_BLOCK: "Command Block",
      COMPARATOR_OFF: "Comparator",
      COMPARATOR_ON: "Comparator",
      CRAFTING_TABLE: "Crafting Table",
      DANDELION: "Dandelion",
      DAYLIGHT_SENSOR: "Daylight Sensor",
      DEADBUSH: "Dead Bush",
      DIAMOND_BLOCK: "Diamond Block",
      DIAMOND_ORE: "Diamond Ore",
      DIRT: "Dirt",
      DIRT_COARSE: "Coarse Dirt",
      DIRT_PODZOL: "Podzol",
      DISPENSER: "Dispenser",
      DOOR_ACACIA: "Acacia Door",
      DOOR_BIRCH: "Brich Door",
      DOOR_DARK_OAK: "Dark Oak Door",
      DOOR_JUNGLE: "Jungle Wood Door",
      DOOR_SPRUCE: "Spruce Door",
      DOOR_WOOD: "Oak Door",
      WOOD_BUTTON: "Wooden Button",
      DOOR_IRON: "Iron Door",
      FENCE: "Fence",
      FENCE_GATE: "Fence Gate",
      DOUBLE_TALLGRASS: "Tall Grass",
      DRAGON_EGG: "Dragon Egg",
      DROPPER: "Dropper",
      EMERALD_BLOCK: "Emerald Block",
      EMERALD_ORE: "Emerald Ore",
      ENCHANTING_TABLE: "Enchanting Table",
      END_BRICKS: "End Bricks",
      END_PORTAL_FRAME: "End Portal Frame",
      END_ROD: "End Rod",
      END_STONE: "End Stone",
      FARMLAND: "Farm Land",
      FERN: "Fern",
      FIRE: "Fire",
      FLOWER_ALLIUM: "Allium Flower",
      FLOWER_CYAN: "Cyan Flower",
      FLOWER_AZURE_BLUET: "Azure Bluet Flower",
      FLOWER_BLUE_ORCHID: "Blue Orchid Flower",
      FLOWER_OXEYE_DAISY: "Oxeye Daisy Flower",
      FLOWER_POT: "Flower Pot",
      FLOWER_TULIP_ORANGE: "Orange Tulip",
      FLOWER_TULIP_PINK: "Pink Tulip",
      FLOWER_TULIP_RED: "Red Tulip",
      FLOWER_TULIP_WHITE: "White Tulip",
      FROSTED_ICE: "Frosted Ice",
      FURNACE_ACTIVE: "Furnace",
      FURNACE_INACTIVE: "Furnace",
      GLASS: "Glass",
      GLASS_PANE: "Glass Pane",
      GLOWSTONE_BLOCK: "Glowstone",
      GOLD_BLOCK: "Gold Block",
      GOLD_ORE: "Gold Ore",  
      GRASS: "Grass",
      GRASS_PATH: "Grass Path",
      GRAVEL: "Gravel",
      HARDENED_CLAY: "Hardened Clay",
      HARDENED_CLAY_STAINED_BLACK: "Black Hardened Clay",
      HARDENED_CLAY_STAINED_BLUE: "Blue Hardened Clay",
      HARDENED_CLAY_STAINED_BROWN: "Brown Hardened Clay",
      HARDENED_CLAY_STAINED_CYAN: "Cyan Hardened Clay",
      HARDENED_CLAY_STAINED_GRAY: "Gray Hardened Clay",
      HARDENED_CLAY_STAINED_GREEN: "Green Hardened Clay",
      HARDENED_CLAY_STAINED_LIGHT_BLUE: "Light Blue Hardened Clay",
      HARDENED_CLAY_STAINED_LIGHT_GRAY: "Light Gray Hardened Clay",
      HARDENED_CLAY_STAINED_LIME: "Lime Hardened Clay",
      HARDENED_CLAY_STAINED_MAGENTA: "Magenta Hardened Clay",
      HARDENED_CLAY_STAINED_ORANGE: "Orange Hardened Clay",
      HARDENED_CLAY_STAINED_PINK: "Pink Hardened Clay",
      HARDENED_CLAY_STAINED_PURPLE: "Purple Hardened Clay",
      HARDENED_CLAY_STAINED_RED: "Red Hardened Clay",
      HARDENED_CLAY_STAINED_WHITE: "White Hardened Clay",
      HARDENED_CLAY_STAINED_YELLOW: "Yellow Hardened Clay",
      HAY_BLOCK: "Hay Block",
      HOPPER: "Hopper",
      ICE: "Ice",
      ICE_PACKED: "Packed Ice",
      IRON_BARS: "Iron Bars",
      IRON_BLOCK: "Iron Block",
      IRON_ORE: "Iron Ore",
      IRON_TRAPDOOR: "Iron Trapdoor",
      ITEM_FRAME: "Item Frame",
      JUKEBOX: "Jukebox",
      LADDER: "Ladder",
      LAPIS_LAZULI_BLOCK: "Lapis Lazuli Block",
      LAPIS_LAZULI_ORE: "Lapis Lazuli Ore",
      LARGE_FERN: "Fern",
      LAVA_FLOWING: "Lava",
      LAVA_STATIONARY: "Lava",
      LEAVES_ACACIA_PERMANENT: "Acacia Leaves",
      LEAVES_ACACIA_DECAYABLE: "Acacia Leaves",
      LEAVES_DARK_OAK_PERMANENT: "Dark Oak Leaves",
      LEAVES_DARK_OAK_DECAYABLE: "Dark Oak Leaves",
      LEAVES_BIRCH_PERMANENT: "Birch Leaves",
      LEAVES_BIRCH_DECAYABLE: "Birch Leaves",
      LEAVES_JUNGLE_PERMANENT: "Jungle Leaves",
      LEAVES_JUNGLE_DECAYABLE: "Jungle Leaves",
      LEAVES_OAK_PERMANENT: "Oak Leaves",
      LEAVES_OAK_DECAYABLE: "Oak Leaves",
      LEAVES_SPRUCE_PERMANENT: "Spruce Leaves",
      LEAVES_SPRUCE_DECAYABLE: "Spruce Leaves",
      LEVER: "Lever",
      LILAC: "Lilac",
      ACACIA_WOOD: "Acacia Wood",
      DARK_OAK_WOOD: "Dark Oak Wood",
      BIRCH_WOOD: "Birch Wood",
      JUNGLE_WOOD: "Jungle Wood",
      STAIRS_WOOD: "Wood Stairs",
      WOOD: "Oak Wood",
      SPRUCE_WOOD: "Spruce Wood",
      MAGMA: "Magma",
      MELON_BLOCK: "Melon",
      MOB_SPAWNER: "Mob Spawner",
      MOSS_STONE: "Moss Stone",
      MUSHROOM_BLOCK_BROWN: "Brown Mushroom",
      MUSHROOM_BLOCK_RED: "Red Mushroom",
      MUSHROOM_BROWN: "Brown Mushroom",
      MUSHROOM_RED: "Red Mushroom",
      MYCELIUM: "Mycelium",
      NETHER_BRICK: "Nether Brick",
      NETHER_WART_BLOCK: "Nether Wart",
      NETHER_WART: "Nether Wart",
      NETHERRACK: "Netherrack",
      NOTEBLOCK: "Note Block",
      OBSIDIAN: "Obsidian",
      PEONY: "Peony",
      PISTON: "Piston",
      PISTON_HEAD: "Piston",
      WOOD_PLANKS_ACACIA: "Acacia Wood Planks",
      WOOD_PLANKS_DARK_OAK: "Dark Oak Wood Planks",
      WOOD_PLANKS_BIRCH: "Birch Wood Planks",
      WOOD_PLANKS_JUNGLE: "Jungle Wood Planks",
      WOOD_PLANKS_OAK: "Oak Wood Planks",
      WOOD_PLANKS_SPRUCE: "Spruce Wood Planks",
      PORTAL: "Portal",
      POTATOES: "Potatoes",
      PRISMARINE_BRICKS: "Prismarine Bricks",
      PRISMARINE_DARK: "Dark Prismarine",
      PRISMARINE: "Prismarine",
      PUMPKIN_INACTIVE: "Pumpkin",
      PUMPKIN_ACTIVE: "Jack O' Lantern",
      PURPUR_BLOCK: "Purpur Block",
      PURPUR_PILLAR: "Purpur Pillar",
      QUARTZ_BLOCK: "Quartz Block",
      QUARTZ_BLOCK_CHISELED: "Chiseled Quartz Block",
      NETHER_QUARTZ_ORE: "Nether Quartz",
      RAIL_ACTIVATOR: "Activator Rail",
      RAIL_DETECTOR: "Detector Rail",
      RAIL_GOLDEN: "Powered Rail",
      RAIL_NORMAL: "Rail",
      RED_NETHER_BRICK: "Nether Brick",
      RED_SAND: "Red Sand",
      RED_SANDSTONE_CHISELED: "Chiseled Red Sandstone",
      RED_SANDSTONE: "Red Sandstone",
      RED_SANDSTONE_SMOOTH: "Smooth Red Standstone",
      REDSTONE_BLOCK: "Redstone Block",
      REDSTONE_LAMP_INACTIVE: "Redstone Lamp",
      REDSTONE_LAMP_ACTIVE: "Redstone Lamp",
      REDSTONE_ORE: "Redstone Ore",
      REDSTONE_TORCH_INACTIVE: "Redstone Torch",
      REDSTONE_TORCH_ACTIVE: "Redstone Torch",
      SUGAR_CANE: "Sugar Cane",
      REDSTONE_REPEATER_INACTIVE: "Redstone Repeater",
      REDSTONE_REPEATER_ACTIVE: "Redstone Repeater",
      ROSE_BUSH: "Rose",
      SAND: "Sand",
      SANDSTONE_CHISELED: "Chiseled Sandstone",
      SANDSTONE: "Sandstone",
      SANDSTONE_SMOOTH: "Smooth Sandstone",
      SAPLING_ACACIA: "Acacia Sapling",
      SAPLING_BIRCH: "Birch Sapling",
      SAPLING_JUNGLE: "Jungle Sapling",
      SAPLING: "Oak Sapling",
      SAPLING_DARK_OAK: "Dark Oak Sapling",
      SAPLING_SPRUCE: "Spruce Sapling",
      SEA_LANTERN: "Sea Lantern",
      SLIME_BLOCK: "Slime Block",
      SNOW: "Snow",
      SNOW_BLOCK: "Snow Block",
      SOUL_SAND: "Soul Sand",
      SPONGE: "Sponge",
      SPONGE_WET: "Sponge",
      STAINED_GLASS_BLACK: "Black Stained Glass",
      STAINED_GLASS_BLUE: "Blue Stained Glass",
      STAINED_GLASS_BROWN: "Brown Stained Glass",
      STAINED_GLASS_CYAN: "Cyan Stained Glass",
      STAINED_GLASS_GRAY: "Gray Stained Glass",
      STAINED_GLASS_GREEN: "Green Stained Glass",
      STAINED_GLASS_LIGHT_BLUE: "Light Blue Stained Glass",
      STAINED_GLASS_LIGHT_GRAY: "Light Gray Stained Glass",
      STAINED_GLASS_LIME: "Lime Stained Glass",
      STAINED_GLASS_MAGENTA: "Magenta Stained Glass",
      STAINED_GLASS_ORANGE: "Orange Stained Glass",
      STAINED_GLASS_PINK: "Pink Stained Glass",
      STAINED_GLASS_PURPLE: "Purple Stained Glass",
      STAINED_GLASS_RED: "Red Stained Glass",
      STAINED_GLASS_WHITE: "White Stained Glass",
      STAINED_GLASS_YELLOW: "Yellow Stained Glass",
      STONE: "Stone",
      STONE_BUTTON: "Stone Button",
      STONE_ANDESITE: "Andesite",
      STONE_ANDESITE_SMOOTH: "Smooth Andesite",
      STONE_DIORITE: "Diorite",
      STONE_DIORITE_SMOOTH: "Smooth Diorite",
      STONE_GRANITE: "Granite",
      STONE_GRANITE_SMOOTH: "Smooth Granite",
      STONE_SLAB: "Stone Slab",
      STONE_SLAB_DOUBLE: "Double Stone Slab",
      STONE_BRICK: "Stone Brick",
      STONE_BRICK_CHISELED: "Chiseled Stone Brick",
      STONE_BRICK_CRACKED: "Cracked Stone Brick",
      STONE_BRICK_MOSSY: "Mossy Stone Brick",
      SUNFLOWER: "Sunflower",  
      GRASS_TALL: "Grass",
      TNT: "TNT",
      TORCH: "Torch",
      TRAPDOOR: "Trapdoor",
      TRIPWIRE: "Tripwire",
      TRIPWIRE_HOOK: "Tripwire Hook",
      VINE: "Vine",
      WATER_FLOWING: "Water",
      WATER_STATIONARY: "Water",
      WATERLILY: "Waterlily",
      COBWEB: "Web",
      WHEAT: "Wheat",
      WOOD_PLANKS_ACACIA: "Acacia Planks",
      WOOD_PLANKS_BIRCH: "Birch Planks",
      WOOD_PLANKS_DARK_OAK: "Dark Oak Planks",
      WOOD_PLANKS_JUNGLE: "Jungle Wood Planks",
      WOOD_PLANKS_OAK: "Oak Planks",
      WOOD_PLANKS_SPRUCE: "Spruce Planks",
      WOOL_BLACK: "Black Wool",
      WOOL_BLUE: "Blue Wool",
      WOOL_BROWN: "Brown Wool",
      WOOL_CYAN: "Cyan Wool",
      WOOL_GRAY: "Gray Wool",
      WOOL_GREEN: "Green Wool",
      WOOL_LIGHT_BLUE: "Light Blue Wool",
      WOOL_LIGHT_GRAY: "Light Gray Wool",
      WOOL_LIME: "Lime Wool",
      WOOL_MAGENTA: "Magenta Wool",
      WOOL_ORANGE: "Orange Wool",
      WOOL_PINK: "Pink Wool",
      WOOL_PURPLE: "Purple Wool",
      WOOL_RED: "Red Wool",
      WOOL_WHITE: "White Wool",
      WOOL_YELLOW: "Blue Wool",
}

Block.toRGBA = {
      AIR: (255, 255, 255, 0),
      ANVIL: (40, 38, 38, 255),
      BEACON: (117, 221, 216, 255, Block.MATERIAL_EMISSIVE),
      BED_OBJECT: (143, 23, 23, 255),
      BED_BLOCK: (143, 23, 23, 255),
      BEDROCK: (84, 84, 84, 255, Block.MATERIAL_STONE),
      BEETROOT: (33, 80, 27, 255),
      BONE_BLOCK: (225, 221, 201, 255),
      BOOKSHELF: (108, 88, 58, 255),
      BREWING_STAND: (58, 48, 38, 255),
      BRICK_BLOCK: (147, 100, 87, 255),
      CACTUS: (12, 93, 22, 255),
      CAKE: (82, 62, 50, 255),
      CARPET_BLACK: (26, 22, 22, 255),
      CARPET_BLUE: (46, 57, 142, 255),
      CARPET_BROWN: (79, 51, 31, 255),
      CARPET_CYAN: (47, 111, 137, 255),
      CARPET_GRAY: (64, 64, 64, 255),
      CARPET_GREEN: (53, 71, 27, 255),
      CARPET_LIGHT_BLUE: (107, 138, 201, 255),
      CARPET_LIGHT_GRAY: (155, 161, 161, 255),
      CARPET_LIME: (66, 174, 57, 255),
      CARPET_MAGENTA: (180, 81, 189, 255),
      CARPET_ORANGE: (219, 125, 63, 255),
      CARPET_PINK: (208, 132, 153, 255),
      CARPET_PURPLE: (127, 62, 182, 255),
      CARPET_RED: (151, 52, 49, 255),
      CARPET_WHITE: (222, 222, 222, 255),
      CARPET_YELLOW: (177, 166, 39, 255),
      CARROTS: (9, 56, 1, 255),
      CAULDRON: (60, 60, 60, 255),
      CHEST: (110, 84, 42, 255),
      CHORUS_FLOWER: (134, 104, 134, 255),
      CHORUS_PLANT: (96, 60, 96, 255),
      CLAY: (159, 164, 177, 255),
      COAL_BLOCK: (19, 19, 19, 255),
      COAL_ORE: (115, 115, 115, 255),
      COCOA_PLANT: (75, 42, 16, 255),
      COBBLESTONE: (123, 123, 123, 255, Block.MATERIAL_STONE),
      STAIRS_COBBLESTONE: (123, 123, 123, 255, Block.MATERIAL_STONE),
      COMMAND_BLOCK: (128, 155, 144, 255),
      COMPARATOR_OFF: (156, 151, 150, 255),
      COMPARATOR_ON: (166, 149, 148, 255),
      CRAFTING_TABLE: (119, 96, 60, 255),
      DANDELION: (13, 20, 0, 255),
      DAYLIGHT_SENSOR: (67, 55, 36, 255),
      DEADBUSH: (40, 25, 8, 255),
      DIAMOND_BLOCK: (98, 219, 214, 255),
      DIAMOND_ORE: (129, 140, 143, 255),
      DIRT: (134, 96, 67, 255),
      DIRT_COARSE: (119, 86, 59, 255),
      DIRT_PODZOL: (123, 88, 57, 255),
      DISPENSER: (117, 117, 117, 255),
      DOOR_ACACIA: (112, 64, 39, 255),
      DOOR_BIRCH: (205, 194, 143, 255),
      DOOR_DARK_OAK: (66, 44, 24, 255),
      DOOR_JUNGLE: (138, 100, 72, 255),
      DOOR_SPRUCE: (97, 76, 50, 255),
      DOOR_WOOD: (134, 102, 51, 255, Block.MATERIAL_WOOD),
      WOOD_BUTTON: (134, 102, 51, 255, Block.MATERIAL_WOOD),
      DOOR_IRON: (164, 164, 164, 255),
      FENCE: (134, 102, 51, 255),
      FENCE_GATE: (134, 102, 51, 255),
      DOUBLE_TALLGRASS: (107, 113, 104, 255),
      DRAGON_EGG: (13, 9, 16, 255),
      DROPPER: (117, 117, 117, 255),
      EMERALD_BLOCK: (81, 217, 117, 255),
      EMERALD_ORE: (110, 129, 116, 255),
      ENCHANTING_TABLE: (31, 33, 35, 255),
      END_BRICKS: (226, 231, 171, 255),
      END_PORTAL_FRAME: (120, 130, 100, 255),
      END_ROD: (56, 49, 52, 255),
      END_STONE: (221, 224, 165, 255, Block.MATERIAL_STONE),
      FARMLAND: (115, 76, 45, 255),
      FERN: (37, 37, 37, 255),
      FIRE: (202, 127, 69, 255, Block.MATERIAL_EMISSIVE),
      FLOWER_ALLIUM: (45, 46, 64, 255),
      FLOWER_CYAN: (0, 255, 255, 255),
      FLOWER_AZURE_BLUET: (51, 68, 70, 255),
      FLOWER_BLUE_ORCHID: (17, 61, 65, 255),
      FLOWER_OXEYE_DAISY: (53, 71, 47, 255),
      FLOWER_POT: (23, 13, 10, 255),
      FLOWER_TULIP_ORANGE: (44, 60, 40, 255),
      FLOWER_TULIP_PINK: (45, 62, 45, 255),
      FLOWER_TULIP_RED: (47, 60, 44, 255),
      FLOWER_TULIP_WHITE: (44, 63, 45, 255),
      FROSTED_ICE: (148,188,255,255),
      FURNACE_ACTIVE: (125,102,85,255, Block.MATERIAL_EMISSIVE),
      FURNACE_INACTIVE: (78,78,78,255),
      GLASS: (60, 67, 68, 64),
      GLASS_PANE: (200, 255, 255, 64),
      GLOWSTONE_BLOCK: (144,118,70,255, Block.MATERIAL_EMISSIVE),
      GOLD_BLOCK: (249,236,79,255),
      GOLD_ORE: (143,140,125,255),  
      GRASS: (127,107,66,255),
      GRASS_PATH: (133,99,65,255),
      GRAVEL: (127,124,123,255),
      HARDENED_CLAY: (151, 93, 67, 255),
      HARDENED_CLAY: (151,93,67,255),  
      HARDENED_CLAY_STAINED_BLACK: (37, 23, 16, 255),
      HARDENED_CLAY_STAINED_BLUE: (74, 60, 91, 255),
      HARDENED_CLAY_STAINED_BROWN: (77, 51, 36, 255),
      HARDENED_CLAY_STAINED_CYAN: (87, 91, 91, 255),
      HARDENED_CLAY_STAINED_GRAY: (58, 42, 36, 255),
      HARDENED_CLAY_STAINED_GREEN: (76, 83, 42, 255),
      HARDENED_CLAY_STAINED_LIGHT_BLUE: (113, 109, 138, 255),
      HARDENED_CLAY_STAINED_LIGHT_GRAY: (135, 107, 98, 255),
      HARDENED_CLAY_STAINED_LIME: (104, 118, 53, 255),
      HARDENED_CLAY_STAINED_MAGENTA: (150, 88, 109, 255),
      HARDENED_CLAY_STAINED_ORANGE: (162, 84, 38, 255),
      HARDENED_CLAY_STAINED_PINK: (162, 78, 79, 255),
      HARDENED_CLAY_STAINED_PURPLE: (118, 70, 86, 255),
      HARDENED_CLAY_STAINED_RED: (143, 61, 47, 255),
      HARDENED_CLAY_STAINED_WHITE: (210, 178, 161, 255),
      HARDENED_CLAY_STAINED_YELLOW: (186, 133, 35, 255),
      HAY_BLOCK: (158,117,18,255),
      HOPPER: (63,63,63,255),
      ICE: (125,173,255,255),
      ICE_PACKED: (165, 195, 245, 255),
      ICE_PACKED: (165,195,245,255),
      IRON_BARS: (50,49,48,255),
      IRON_BLOCK: (219, 219, 219, 255),
      IRON_ORE: (136, 130, 127, 255),
      IRON_TRAPDOOR: (172,172,172,255),
      ITEM_FRAME: (118,68,45,255),
      JUKEBOX: (101,68,51,255),
      LADDER: (68,54,30,255),
      LAPIS_LAZULI_BLOCK: (39, 67, 138, 255),
      LAPIS_LAZULI_ORE: (102, 112, 135, 255),
      LARGE_FERN: (79, 79, 79, 255),
      LAVA_FLOWING: (207,92,20,255, Block.MATERIAL_EMISSIVE),
      LAVA_STATIONARY: (212,90,18,255, Block.MATERIAL_EMISSIVE),
      LEAVES_ACACIA_PERMANENT: (0,82,0,255, Block.MATERIAL_ROUGH),
      LEAVES_ACACIA_DECAYABLE: (0,82,0,255, Block.MATERIAL_ROUGH),
      LEAVES_DARK_OAK_PERMANENT: (0,82,0,255, Block.MATERIAL_ROUGH),
      LEAVES_DARK_OAK_DECAYABLE: (0,82,0,255, Block.MATERIAL_ROUGH),
      LEAVES_BIRCH_PERMANENT: (0,82,0,255, Block.MATERIAL_ROUGH),
      LEAVES_BIRCH_DECAYABLE: (0,82,0,255, Block.MATERIAL_ROUGH),
      LEAVES_JUNGLE_PERMANENT: (0,113,0,255, Block.MATERIAL_ROUGH),
      LEAVES_JUNGLE_DECAYABLE: (0,113,0,255, Block.MATERIAL_ROUGH),
      LEAVES_OAK_PERMANENT: (0,82,0,255, Block.MATERIAL_ROUGH),
      LEAVES_OAK_DECAYABLE: (0,82,0,255, Block.MATERIAL_ROUGH),
      LEAVES_SPRUCE_PERMANENT: (0,75,0,255, Block.MATERIAL_ROUGH),
      LEAVES_SPRUCE_DECAYABLE: (0,75,0,255, Block.MATERIAL_ROUGH),
      LEVER: (8,7,5,255),
      LILAC: (70, 85, 64, 255),
      ACACIA_WOOD: (105,99,89,255, Block.MATERIAL_WOOD),
      DARK_OAK_WOOD: (52,41,23,255, Block.MATERIAL_WOOD),
      BIRCH_WOOD: (207,206,201,255, Block.MATERIAL_WOOD),
      JUNGLE_WOOD: (87,68,27,255, Block.MATERIAL_WOOD),
      STAIRS_WOOD: (102,81,50,255, Block.MATERIAL_WOOD),
      WOOD: (102,81,50,255, Block.MATERIAL_WOOD),
      SPRUCE_WOOD: (46,29,12,255, Block.MATERIAL_WOOD),
      MAGMA: (135,66,26,255),
      MELON_BLOCK: (141,146,36,255),
      MOB_SPAWNER: (16,24,30,255),
      MOSS_STONE: (104, 121, 104, 255, Block.MATERIAL_STONE),
      MUSHROOM_BLOCK_BROWN: (142,107,83,255),
      MUSHROOM_BLOCK_RED: (183,38,36,255),
      MUSHROOM_BROWN: (14,11,9,255),
      MUSHROOM_RED: (26,7,8,255),
      MYCELIUM: (114,88,74,255),
      NETHER_BRICK: (45,23,27,255),
      NETHER_WART_BLOCK: (117, 6, 7, 255),
      NETHER_WART: (89,15,14,255),
      NETHERRACK: (111, 54, 53, 255),
      NOTEBLOCK: (101, 68, 51, 255),
      OBSIDIAN: (20, 18, 30, 255),
      PEONY: (53, 64, 37, 255),
      PISTON: (107,102,95,255),
      PISTON_HEAD: (154,130,90,255),
      WOOD_PLANKS_ACACIA: (169,92,51,255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_DARK_OAK: (61,40,18,255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_BIRCH: (196,179,123,255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_JUNGLE: (154,110,77,255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_OAK: (157,128,79,255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_SPRUCE: (104,78,47,255, Block.MATERIAL_WOOD),
      PORTAL: (87,11,191,255),
      POTATOES: (14,71,15,255),
      PRISMARINE_BRICKS: (100, 160, 143, 255),
      PRISMARINE_DARK: (60, 88, 75, 255),
      PRISMARINE: (100,152,142,255),
      PUMPKIN_INACTIVE: (142,77,13,255),
      PUMPKIN_ACTIVE: (185,133,28,255, Block.MATERIAL_EMISSIVE),
      PURPUR_BLOCK: (166,122,166,255),
      PURPUR_PILLAR: (170,126,170,255),
      QUARTZ_BLOCK: (236, 233, 226, 255),
      QUARTZ_BLOCK_CHISELED: (232,228,220,255),
      NETHER_QUARTZ_ORE: (125,85,80,255),
      RAIL_ACTIVATOR: (77,62,46,255),
      RAIL_DETECTOR: (73,62,54,255),
      RAIL_GOLDEN: (89,73,49,255),
      RAIL_NORMAL: (68,61,50,255),
      RED_NETHER_BRICK: (68, 4, 7, 255),
      RED_SAND: (169,88,33,255),
      RED_SANDSTONE_CHISELED: (162,83,28,255, Block.MATERIAL_STONE),
      RED_SANDSTONE: (166,85,30,255, Block.MATERIAL_STONE),
      RED_SANDSTONE_SMOOTH: (168, 86, 31, 255, Block.MATERIAL_STONE),
      REDSTONE_BLOCK: (171, 28, 9, 255, Block.MATERIAL_EMISSIVE),
      REDSTONE_LAMP_INACTIVE: (70,43,27,255),
      REDSTONE_LAMP_ACTIVE: (119,89,55,255, Block.MATERIAL_EMISSIVE),
      REDSTONE_ORE: (133,107,107,255),
      REDSTONE_TORCH_INACTIVE: (7,5,3,255),
      REDSTONE_TORCH_ACTIVE: (17,8,4,255, Block.MATERIAL_EMISSIVE),
      SUGAR_CANE: (82,106,56,255),
      REDSTONE_REPEATER_INACTIVE: (151,147,147,255),
      REDSTONE_REPEATER_ACTIVE: (161,147,147,255, Block.MATERIAL_EMISSIVE),
      ROSE_BUSH: (51, 50, 3, 255),
      SAND: (219,211,160,255),
      SANDSTONE_CHISELED: (216,208,155,255, Block.MATERIAL_STONE),
      SANDSTONE: (217,210,157,255, Block.MATERIAL_STONE),
      SANDSTONE_SMOOTH: (220,212,162,255, Block.MATERIAL_STONE),
      SAPLING_ACACIA: (41,46,9,255,Block.MATERIAL_WOOD),
      SAPLING_BIRCH: (50,64,36,255,Block.MATERIAL_WOOD),
      SAPLING_JUNGLE: (16,29,6,255,Block.MATERIAL_WOOD),
      SAPLING: (30,43,16,255,Block.MATERIAL_WOOD),
      SAPLING_DARK_OAK: (32,50,16,255,Block.MATERIAL_WOOD),
      SAPLING_SPRUCE: (17,19,11,255,Block.MATERIAL_WOOD),
      SEA_LANTERN: (172,200,190,255,Block.MATERIAL_EMISSIVE),
      SLIME_BLOCK: (121, 200, 101, 255),
      SNOW: (240,251,251,255),
      SNOW_BLOCK: (149, 121, 97, 255),
      SOUL_SAND: (85, 64, 52, 255),
      SOUL_SAND: (85,64,52,255),
      SPONGE: (195,196,85,255,Block.MATERIAL_ROUGH),
      SPONGE_WET: (160,159,63,255,Block.MATERIAL_ROUGH),
      STAINED_GLASS_BLACK: (25, 25, 25, 64),
      STAINED_GLASS_BLUE: (51, 76, 178, 64),
      STAINED_GLASS_BROWN: (102, 76, 51, 64),
      STAINED_GLASS_CYAN: (76, 127, 153, 64),
      STAINED_GLASS_GRAY: (76, 76, 76, 64),
      STAINED_GLASS_GREEN: (102, 127, 51, 64),
      STAINED_GLASS_LIGHT_BLUE: (102, 153, 216, 64),
      STAINED_GLASS_LIGHT_GRAY: (153, 153, 153, 64),
      STAINED_GLASS_LIME: (127, 204, 25, 64),
      STAINED_GLASS_MAGENTA: (178, 76, 216, 64),
      STAINED_GLASS_ORANGE: (216, 127, 51, 64),
      STAINED_GLASS_PINK: (242, 127, 165, 64),
      STAINED_GLASS_PURPLE: (127, 63, 178, 64),
      STAINED_GLASS_RED: (153, 51, 51, 64),
      STAINED_GLASS_WHITE: (255, 255, 255, 64),
      STAINED_GLASS_YELLOW: (229, 229, 51, 64),
      STONE: (125, 125, 125, 255, Block.MATERIAL_STONE),
      STONE_BUTTON: (125,125,125,255, Block.MATERIAL_STONE),
      STONE_ANDESITE: (131, 131, 131, 255, Block.MATERIAL_STONE),
      STONE_ANDESITE: (131,131,131,255, Block.MATERIAL_STONE),
      STONE_ANDESITE_SMOOTH: (133, 133, 135, 255, Block.MATERIAL_STONE),
      STONE_ANDESITE_SMOOTH: (133,133,135,255, Block.MATERIAL_STONE),
      STONE_DIORITE: (180, 180, 183, 255, Block.MATERIAL_STONE),
      STONE_DIORITE: (180,180,183,255, Block.MATERIAL_STONE),
      STONE_DIORITE_SMOOTH: (183, 183, 186, 255, Block.MATERIAL_STONE),
      STONE_DIORITE_SMOOTH: (183,183,186,255, Block.MATERIAL_STONE),
      STONE_GRANITE: (153, 114, 99, 255, Block.MATERIAL_STONE),
      STONE_GRANITE: (153,114,99,255, Block.MATERIAL_STONE),
      STONE_GRANITE_SMOOTH: (159, 115, 98, 255, Block.MATERIAL_STONE),
      STONE_GRANITE_SMOOTH: (159,115,98,255, Block.MATERIAL_STONE),
      STONE_SLAB: (167,167,167,255, Block.MATERIAL_STONE),
      STONE_SLAB_DOUBLE: (167,167,167,255, Block.MATERIAL_STONE),
      STONE_BRICK: (122,122,122,255, Block.MATERIAL_STONE),
      STONE_BRICK_CHISELED: (119,119,119,255, Block.MATERIAL_STONE),
      STONE_BRICK_CRACKED: (119,119,119,255, Block.MATERIAL_STONE),
      STONE_BRICK_MOSSY: (115,119,106,255, Block.MATERIAL_STONE),
      SUNFLOWER: (104, 100, 19, 255),  
      GRASS_TALL: (86,86,86,255),
      TNT: (170,93,71,255),
      TORCH: (10,8,5,255),
      TRAPDOOR: (145,116,75,255),
      TRIPWIRE: (21,21,21,255),
      TRIPWIRE_HOOK: (25,23,20,255),
      VINE: (60,60,60,255),
      WATER_FLOWING: (49,72,244,200),
      WATER_STATIONARY: (47,67,244,200),
      WATERLILY: (69,69,69,255),
      COBWEB: (90,90,90,255),
      WHEAT: (57,67,5,255),
      WOOD_PLANKS_ACACIA: (169, 92, 51, 255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_BIRCH: (196, 179, 123, 255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_DARK_OAK: (61, 40, 18, 255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_JUNGLE: (154, 110, 77, 255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_OAK: (157, 128, 79, 255, Block.MATERIAL_WOOD),
      WOOD_PLANKS_SPRUCE: (104, 78, 47, 255, Block.MATERIAL_WOOD),
      WOOL_BLACK: (26, 22, 22, 255),
      WOOL_BLUE: (46, 57, 142, 255),
      WOOL_BROWN: (79, 51, 31, 255),
      WOOL_CYAN: (47, 111, 137, 255),
      WOOL_GRAY: (64, 64, 64, 255),
      WOOL_GREEN: (53, 71, 27, 255),
      WOOL_LIGHT_BLUE: (107, 138, 201, 255),
      WOOL_LIGHT_GRAY: (155, 161, 161, 255),
      WOOL_LIME: (66, 174, 57, 255),
      WOOL_MAGENTA: (180, 81, 189, 255),
      WOOL_ORANGE: (219, 125, 63, 255),
      WOOL_PINK: (208, 132, 153, 255),
      WOOL_PURPLE: (127, 62, 182, 255),
      WOOL_RED: (151, 52, 49, 255),
      WOOL_WHITE: (222, 222, 222, 255),
      WOOL_YELLOW: (177, 166, 39, 255),
    }
