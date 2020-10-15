#!/usr/bin/env bash

# *
# * (c) Copyright IBM Corporation. 2019
# * SPDX-License-Identifier: Apache-2.0
# * By Kip Twitchell

echo '*****************************************************'
echo '************* install GitHub and Clone UL ***********'

sudo yum -y install git
cd /

# Authentication required if not in a public github repository
sudo git clone https://github.ibm.com/kip-twitchell/universal_ledger.git

