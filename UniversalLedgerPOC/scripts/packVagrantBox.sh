#!/usr/bin/env bash

# This script creates a new vagrant box from whatever the status of the current box is.
# *
# * (c) Copyright IBM Corporation. 2019
# * SPDX-License-Identifier: Apache-2.0
# * By Kip Twitchell

echo '*****************************************************'
echo '************* Package up the Vagrant Box  ***********'

# This lists the current boxes, in order to get the actual box name
vboxmanage list vms

# the name from the list above has to be inserted into this command to actually package the box
vagrant package --base universal_ledger_ulserver_1563831974277_50052 --output universal_ledger_base.box

