class Entity:
    """Minecraft PI entity class"""

    def __init__(self, instance_id, name, id=0, nbt=None):
        self.id = id
        self.name = name
        self.instance_id = instance_id
        if nbt is not None and len(nbt)==0:
            self.nbt = None
        else:
            self.nbt = nbt

    def __eq__(self, rhs):
        try:
            return self.id == rhs.id and self.name == rhs.name and self.nbt == rhs.nbt
        except:
            return self.name == rhs.name and self.nbt is None and self.id == rhs

    def __ne__(self, rhs):
        return not self.__eq__(rhs)

    def __hash__(self):
        h = (self.id << 8) + self.name
        if self.nbt is not None:
            h ^= hash(self.nbt)

    def getName(self):
        return self.name
        
    def getId(self):
        return self.id
        
    def getEntityId(self):
        return self.instance_id

    def __iter__(self):
        """Allows a Entity to be sent whenever id [and name] is needed"""
        if self.nbt is not None:
           return iter((self.instance_id, self.id, self.name, self.nbt))
        else:
           return iter((self.instance_id, self.id, self.name))

    def __hash__(self):
        return hash((self.instance_id, self.id, self.name, self.nbt))

    def __repr__(self):
        if self.nbt is None:
            return "Entity(%d, %d, %s)"%(self.instance_id, self.id, self.name)
        else:
            return "Entity(%d, %d, %s, %s)"%(self.instance_id, self.id, self.name, repr(self.nbt))


ITEM = Entity(1, "Item")
XPORB = Entity(2, "XPOrb")
LEASHKNOT = Entity(8, "LeashKnot")
PAINTING = Entity(9, "Painting")
ARROW = Entity(10, "Arrow")
SNOWBALL = Entity(11, "Snowball")
FIREBALL = Entity(12, "Fireball")
SMALLFIREBALL = Entity(13, "SmallFireball")
THROWNENDERPEARL = Entity(14, "ThrownEnderpearl")
EYEOFENDERSIGNAL = Entity(15, "EyeOfEnderSignal")
THROWNPOTION = Entity(16, "ThrownPotion")
THROWNEXPBOTTLE = Entity(17, "ThrownExpBottle")
ITEMFRAME = Entity(18, "ItemFrame")
WITHERSKULL = Entity(19, "WitherSkull")
PRIMEDTNT = Entity(20, "PrimedTnt")
FALLINGSAND = Entity(21, "FallingSand")
FIREWORKSROCKETENTITY = Entity(22, "FireworksRocketEntity")
ARMORSTAND = Entity(30, "ArmorStand")
BOAT = Entity(41, "Boat")
MINECARTRIDEABLE = Entity(42, "MinecartRideable")
MINECARTCHEST = Entity(43, "MinecartChest")
MINECARTFURNACE = Entity(44, "MinecartFurnace")
MINECARTTNT = Entity(45, "MinecartTNT")
MINECARTHOPPER = Entity(46, "MinecartHopper")
MINECARTSPAWNER = Entity(47, "MinecartSpawner")
MINECARTCOMMANDBLOCK = Entity(40, "MinecartCommandBlock")
MOB = Entity(1, "Mob")
MONSTER = Entity(1, "Monster")
CREEPER = Entity(50, "Creeper")
SKELETON = Entity(51, "Skeleton")
SPIDER = Entity(52, "Spider")
GIANT = Entity(53, "Giant")
ZOMBIE = Entity(54, "Zombie")
SLIME = Entity(55, "Slime")
GHAST = Entity(56, "Ghast")
PIGZOMBIE = Entity(57, "PigZombie")
ENDERMAN = Entity(58, "Enderman")
CAVESPIDER = Entity(59, "CaveSpider")
SILVERFISH = Entity(60, "Silverfish")
BLAZE = Entity(61, "Blaze")
LAVASLIME = Entity(62, "LavaSlime")
ENDERDRAGON = Entity(63, "EnderDragon")
WITHERBOSS = Entity(64, "WitherBoss")
BAT = Entity(65, "Bat")
WITCH = Entity(66, "Witch")
ENDERMITE = Entity(67, "Endermite")
GUARDIAN = Entity(68, "Guardian")
PIG = Entity(90, "Pig")
SHEEP = Entity(91, "Sheep")
COW = Entity(92, "Cow")
CHICKEN = Entity(93, "Chicken")
SQUID = Entity(94, "Squid")
WOLF = Entity(95, "Wolf")
MUSHROOMCOW = Entity(96, "MushroomCow")
SNOWMAN = Entity(97, "SnowMan")
OCELOT = Entity(98, "Ocelot")
VILLAGERGOLEM = Entity(99, "VillagerGolem")
HORSE = Entity(100, "EntityHorse")
RABBIT = Entity(101, "Rabbit")
VILLAGER = Entity(120, "Villager")
ENDERCRYSTAL = Entity(200, "EnderCrystal")
