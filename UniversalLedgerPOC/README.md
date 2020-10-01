# Universal Ledger

// (c) Copyright IBM Corporation 2020.  
//     Copyright Contributors to the GenevaERS Project.*
// SPDX-License-Identifier: Apache-2.0
//
// ***************************************************************************
//                                  *                                         
//   Licensed under the Apache License, Version 2.0 (the "License");         
//   you may not use this file except in compliance with the License.        
//   You may obtain a copy of the License at                                 
//                                                                           
//     http://www.apache.org/licenses/LICENSE-2.0                            
//                                                                           
//   Unless required by applicable law or agreed to in writing, software     
//   distributed under the License is distributed on an "AS IS" BASIS,       
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and     
//   limitations under the License.                                          
// ***************************************************************************

This project involved IBM SAFR team members building on other POC's created to use the concepts of an Instrument Ledger and demonstrate how many more attributes can be maintained in a new type of ledger.

The purpose of this ledger can be understood from this white paper: https://ledgerlearning.com/whitepapers/minimum-costs-for-financial-system-data-maintenance/

# Functions

This ledger uses publicly released State of Virginia Purchase Order and Payment data to demonstrate these concepts.

(1) Raw transaction assignment to unique involved party IDs

(2) Creation of Universal Journal Entries for these transactions, with a simple posting model of all offset going to cash.

(3) Update of balances through use of Common Key Buffering concepts pioneered in SAFR.  https://ledgerlearning.com/books/balancing-act-financial-systems-textbook/on-line-balancing-act-text-book/part-5-the-programmer/chapter-52-common-key-data-buffering/

(4) Production of multiple output from a single pass of the data file

# Technologies

This project uses the following technologies:

(1) Vagrant as the VM container

(2) CentOS as the Linux Distro (chosen because of the database to be used)

(3) Scala as the development language

(4) sbt as the build manager for Scala

(5) optional Play Framework web server

(6) optional Spark instance

(7) optional Derby Database instance

(8) optional PostgreSQL Database instance

# Initial Setup

The project can be used by doing the following:

(1) Download and install Vagrant https://www.vagrantup.com/downloads.html

(2) Replicate this git project to your hard disk

(3) At a command prompt within the root directory of this project, (the directory that contains the Vagrantfile)

(a) Install Vagrant Virtual Box Guest Additions by typing (this step is not tested)

    vagrant plugin install vagrant-vbguest

    and then

    vagrant vbguest

    (if you receive a message about "Vagrant was unable to mount VirtualBox shared folders. This is usually because the filesystem "vboxsf" is not available." then use the command "vagrant vbguest --status" to see if something is wrong with this installation)

(b) Start the virtual box by typing

    vagrant up

(4) When that is complete, type "vagrant ssh" which will put you inside the vagrant VM.

(5) change directory to /universal_ledger/scripts

(6) use the bash script to initial the java, scala and sbt and Play Framework web server environments, by typing "bash ./initenv.sh"
 This script will

    (a) Update the OS upon initial startup
    (b) download wget for use in later commands
    (c) download and install Java 8
    (d) download and install Scala 2.11 (to be potentially compatible with Spark if needed)
    (e) install sbt
    (f) start-up the Play Framework server.
    (g) Once started, you can test if it is working by going to http://127.0.0.1:9000 on your host


(7) after performing the above, to initial the Spark environment type "bash ./sparkinit.sh"
 This script will

     (a) changes to the /home/vagrant directory
     (b) download and unpacks spark 2.4.1
     (c) sets the SPARK_HOME environment variable
     (d) confirms installation by showing the spark version
     (e) changes to the /universal_ledger/SAFRonSpark directory
     (f) issues the "sbt package" to compile and build the Jar File
     (g) runs the SAFRonSpark SimpleSQL test script by running "bash /universal_ledger/SAFRonSpark/data/InitEnv/SimpleTest1.sh

 (8) Once both of them have completed, refresh your bash profile, for example by exiting Vagrant ssh and re-enter it

 # Teardown

 The above script can be used to reset the environment if one frist destroys the vagrant environment.  To do this:

 (0) if you are at the sbt prompt, you need to hit control-C to get back to Vagarnt ssh prompt

 (1) Type "Exit" to exit the VM shell environment

 (2) Within the VagrantFile directory, type "vagrant destroy"

 (3) Confirm the choice to destroy the vagrant environment.

 (4) To rebuild the environment start again with the "Vagrant up" command.


 #Subprojects

 This git project includes three subprojects:


`Record Read: (gen:1),(exp:291074748),(bins:(address:Mountain View, CA 94043),(name:Aerospike, Inc.),(email:info@aerospike.com))`
`Record Written: (gen:1),(exp:291075028),(bins:(bin1:Initial Test),(bin2:of Scala Insert))`
`[success] Total time: 2 s, completed Mar 18, 2019 10:10:28 PM`

######  _Play Framework_

 Play Framework has replaced the original Scalatra server in the project (see archive repo at: https://github.ibm.com/kip-twitchell/VagrantScalatraSparkBase)

 To build and start the webserver:

 (1) cd /universal_ledger/play/

 (2) type "sbt" and hit enter

 (3) type "run" and hit enter

 (4) when the message says the server has started, point your browser to http://localhost:9000


###### _Derby Database_

The Derby Database is a possible alternative to Aerospike. To install it;

 (1) cd /universal_ledger/scripts/

 (2) use the bash script by typing "bash ./derbyinit.sh" and hit enter.  This script does the following:

    (a) changes to the /home/vagrant directory
    (b) download and unpacks Derby database
    (c) sets the DERBY_HOME environment variable
    (d) sets the CLASS_PATH using a Derby provided script
    (d) confirms installation by showing the Derby version

(3) To run Derby Interactively, type "java org.apache.derby.tools.ij"

###### _Universal Ledger_

Scripts, documentation, data and common build.sbt parameters are held at the Universal_Ledger project level.  There are currently no dependencies between subprojects, and nothing to build at the project level.  

# Multi Server

The basic Vagrant Configuration was updated to potentially create multiple VM servers, allowing for a true Distributed Ledger Configuration. Server (Hostnames) include:

    (1) ulserver
    (2) ulclient1
    (3) ulclient2

The default server is ulserver.

If no server is specified on a Vagrant command (i.e, ”vagrant ssh ulcienta”) then ulserver will be affected Environment testing code is set to talk to ulserver

Ulclient1 and 2 are not automatically started, use ”vagrant up ulcienta” to start them.

Aerospike (Port 3000) and Playframework (9000) access from host to ulserver worked fine on my machine with the port forwarded commands. Other server port conflicts are shown and fixed automatically by vagrant on start up

The synced folder can be accessed by all servers

# Issues

The PostgreSQL install process works, but the scripts for establishing the user IDs have not been finished  

# Useful Commands
The following are useful commands for doing Agile (interactive) development.  
   - Spark can be started by going to the home directory (home/vagrant/spark....) and typing "./bin/spark-shell"
   - Play Framework can be started by going to /universal_ledger/play and typing "sbt".  After sbt starts, type "run".  Automatic reloading of files doesn't seem to always work, so simply hitting enter at the console stops the server; restarting it causes recompile.

# Legal

 Each source file must include a license header for the Apache Software License 2.0. Using the SPDX format is the simplest approach. e.g.

` /*`
`  Copyright <holder> All Rights Reserved.`
` `
`  SPDX-License-Identifier: Apache-2.0
`
`  */`

 We have tried to make it as easy as possible to make contributions. This applies to how we handle the legal aspects of contribution. We use the same approach - the Developer's Certificate of Origin 1.1 (DCO) - that the Linux® Kernel community uses to manage code contributions.

 We simply ask that when submitting a patch for review, the developer must include a sign-off statement in the commit message.

 Here is an example Signed-off-by line, which indicates that the submitter accepts the DCO:

 Signed-off-by: John Doe <john.doe@example.com>
 You can include this automatically when you commit a change to your local git repository using the following command:

 `git commit -s`
