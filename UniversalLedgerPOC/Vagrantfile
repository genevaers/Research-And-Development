# -*- mode: ruby -*-
# vi: set ft=ruby :

# (c) Copyright IBM Corporation. 2018
# SPDX-License-Identifier: Apache-2.0
# By Kip Twitchell

# This Vagrant configuration file begins with an CentOS Linux Distro Vagrant container
# It potentially creates three independent servers, but only the first is started automatically,
#   and it is the default (if no server is specified on a vagrant command, it defaults to the ulserver
# the script directory contains an initenv.sh which will complete the scala development
#   set up in the environment

Vagrant.configure("2") do |config|

  # These are the vbguest additions for syncing folders
  config.vbguest.auto_update = false
  config.vbguest.no_remote = true

  config.vm.define :ulserver, primary: true do |ulserver|
  #  ulserver.vm.box = "ktwitchell001/Universal_ledger"
    ulserver.vm.box = "geerlingguy/centos7"
  #  ulserver.vm.network :private_network, ip: "192.168.10.200"
    ulserver.vm.network :private_network, type: "dhcp"
    ulserver.vm.hostname = "ulserver"
    ulserver.vm.synced_folder ".", # <--- this directory for code
        "/universal_ledger",
        id: "code", type: "virtualbox"
  ######  enabled for postgreSQL Subproject use
    ulserver.vm.network "forwarded_port", guest: 5432, host: 5432, auto_correct: true
  ######  enabled for Play framework
    ulserver.vm.network "forwarded_port", guest: 9000, host: 9000, auto_correct: true
    # the following (and in other boxes below) creates a simple hostname file,
    # This allows use of hostname in commands in the vm to talk to the other servers
    config.vm.provision "shell", inline: <<-SHELL
    SHELL
  end

   config.vm.define :ulclient1, autostart: false do |ulclient1|
     ulclient1.vm.box = "geerlingguy/centos7" # <- not tested
   #  ulclient1.vm.network :private_network, ip: "192.168.10.201"
     ulclient1.vm.network :private_network, type: "dhcp"
     ulclient1.vm.hostname = "ulclient1"
     ulclient1.vm.synced_folder ".", "/universal_ledger", type: "virtualbox"
     config.vm.provision "shell", inline: <<-SHELL
     SHELL
   end

  config.vm.define :ulclient2, autostart: false do |ulclient2|
     ulclient2.vm.box = "geerlingguy/centos7" # <- not tested
   #  ulclient2.vm.network :private_network, ip: "192.168.10.202"
     ulclient2.vm.network :private_network, type: "dhcp"
     ulclient2.vm.hostname = "ulclient2"
     ulclient2.vm.synced_folder ".", "/universal_ledger", type: "virtualbox"
     config.vm.provision "shell", inline: <<-SHELL
     SHELL
   end
end