# Roboticraft [![Build Status](https://travis-ci.org/DomAmato/Robot.svg?branch=master)](https://travis-ci.org/DomAmato/Robot) [![Documentation Status](https://readthedocs.org/projects/robot/badge/?version=latest)](http://robot.readthedocs.io/en/latest/?badge=latest) 
Programmable Robot Entities for Minecraft.

## Info
The robots are programmed using Python which should be installed on the system. If using Windows, an embedded version can be used and the mod will download and unpack the embeddable version if it is enabled in the config. It also unpacks an API which is based off of the Mojang Python API from the Raspberry Pi version though heavily modified to prevent game breaking abilities. The API was inspired by the Raspberry Jam Mod though made to be more extensible for creating addons.

### The mod requires the Rabbit GUI mod client side 
[Code Repo Here](https://github.com/CityOfLearning/rabbit-gui/tree/1.12)

[Download Jar](https://github.com/CityOfLearning/rabbit-gui/releases/download/1.12.2.1/rabbit-gui.jar)

## Items and Crafting
The robot has a lot of functionality but it needs the proper expansion chips otherwise it doesn't know the commands.
### Expansion Chips
* Attack Chip

   Allows the robot to attack entities assuming it has a sword equipped. An entity ID must be passed as an argument and really the best way to find those is using the detection chip

![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/attack.png "Attack Chip")

* Build Chip

   Enables the robot to build using material from its inventory, will use it in order of slot unless specified which block to use.
   

![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/build.png "Build Chip")

* Climb Chip

   Enables the robot to climb ladders and vines. Will also climb steps 1 block high although the new walk processor also allows the robot to do that without explicitly telling it to.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/climb.png "Climbing Chip")

* Detection Chip

   Enables the robot to detect nearby entities and returns a list of all that were found. Can be enchanded with the power enchantment to increase the range by 10 per level. Works really well with the attacking chip or as a sentry that can chat with the player.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/detection.png "Detection Chip")

* Inspection Chip

   Enables the robot to inspect the surrounding blocks returning the type found at that location. Really useful for pathing and mining.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/inspection.png "Inspection Chip")

* Interaction Chip

   Enables the robot to interact with simple redstone components, buttons, levers, doors, trapdoors. 
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/interact.png "Interaction Chip")

* Jump Chip

   Enables the robot to jump over a 1 block gap. Careful the robot will blindly jump whatever way it is facing so if the gap is too large it could plummet to its death. 
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/jump.png "Jump Chip")


* Mining Chip

   What kind of robot in Minecraft would not have the ability to mine blocks? As long as it has inventory space blocks will go into the inventory, after that it will just drop them on the ground.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/mining.png "Mining Chip")

### Other Items
* Remote

   The remote is the most important part of interacting with the robots, it is needed to activate them and get to the programming screen.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/remote.png "Robot Remote")

* Redstone Meter

   Equipping this will enable the robot to be activated using a redstone signal. It will execute whatever code is currently saved to the SD card that is equipped.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/meter.png "Redstone Meter")

* RAM

   The robot only has a basic memory, you can expand the number of lines that can be processed by adding more RAM.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/ram.png "RAM")

* Robot

   Currently the robot doesn't spawn in the world it must be crafted and activated.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/robot_block.png "Robot")


* SD Card

   You can save your program to these SD cards, it will write the code to your computer and is also an exchangable item. Want to share your program with someone else? Save it to a card and give it to another player and the program will transfer over to them too.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/sd_card.png "SD Card")


* SIM Card

   Using your remote you can connect to robots within a 64 block area. Need to communicate with robots in different dimensions or over great distances? Use a SIM card.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/sim_card.png "SIM Card")

* Wrench

   Robots are weak but they are mostly immune to players attacks. Need to remove a robot you own? You will need a wrench.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/wrench.png "Wrench")

* Robot API Manual

   Need some help remembering how the API works, make a manual for reference.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/manual.png "Robot API Manual")
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/gui/manual2.png "Robot API Manual")

* Magnet Block

   Warp over nearby robots to this block facing the set direction. Useful for setting inital starting direction and position.
   
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/magnet.png "Magnet Block")

## GUIs
The GUIs are mostly built off of the Rabbit GUI Library so again this mod will not work without it.

### Robot Interfaces
Once you first create your robot and need to activate it you can give it a name. Otherwise a random one is generated.
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/gui/activate.png "Activation Gui")

The robot has an inventory, it has slots for various tools as well as the expansion chips. Note the information on the right will let you know what functions it has and other information.
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/gui/inventory.png "Robots Inventory")

The big component of the mod is programming so the mod includes a native IDE with code highlighting and some simple suggestions. You can drag the screen around to get a better view and if you mouse off the interface it goes semi transparent.
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/gui/program1.png "Programming Interface")

Once you have multiple robots activated remember their names as you can use the remote to connect them.
![alt text](https://github.com/CityOfLearning/Robot/blob/master/images/gui/remote.png "Remote Interface")

## Good luck!
### If you find a bug please report it here
