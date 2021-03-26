#!/usr/bin/env bash

# This script download VA data to the vagrant environment
# *
# * (c) Copyright IBM Corporation. 2019
# * SPDX-License-Identifier: Apache-2.0
# * By Kip Twitchell

echo '*****************************************************'
echo '************* download and unpack VAData ************'

# sudo yum -y install git
# cd /home/vagrant
# git clone https://github.com/KipTwitchell/State_of_VA_Data_Compressed.git

cd /home/vagrant
sudo yum -y install unzip

#############################################################################################
# payment data
#############################################################################################
mkdir VAData
cd VAData

# Data Set 1

mkdir payment
cd payment

wget https://query.data.world/s/dqz6oldokj7gemvn52rs6jyhfwjvbn -O FY03q1exp.txt.zip
wget https://query.data.world/s/xbvp4lfopukawte32wic6c2wxq6zmh -O FY03q2exp.txt.zip
wget https://query.data.world/s/rm4y5ouztlqo7qvbtn67rio4bsi5m2 -O FY03q3exp.txt.zip
wget https://query.data.world/s/dhs6sjzae6ekcugugmohmsg5rfzepn -O FY03q4exp.txt.zip
wget https://query.data.world/s/lubobcanp4zkmyi6z4nbegjaubghyp -O FY03rev.txt.zip
wget https://query.data.world/s/ishz2y7ztu7cqxsburqow2h5cwaljx -O FY04q1exp.txt.zip
wget https://query.data.world/s/zco6oiq2nwu22tzlr3exngetuwrtp2 -O FY04q2exp.txt.zip
wget https://query.data.world/s/gvhtgvhpurgvubwe5qvnpzfayxrfnl -O FY04q3exp.txt.zip
wget https://query.data.world/s/ytmv6c2arx5p2nlidx5shzc7nilh7z -O FY04q4exp.txt.zip
wget https://query.data.world/s/kgz7lpaawvbqz4njnhppsh6pa3yrsh -O FY04rev.txt.zip
wget https://query.data.world/s/6iauvhasaaho76hvg6gufbrkskqr6y -O FY05q1exp.txt.zip
wget https://query.data.world/s/zuev2ujfpuwga723btlwambipevyp5 -O FY05q2exp.txt.zip
wget https://query.data.world/s/lzvrddhndgikisp2msk457zv5sebcs -O FY05q3exp.txt.zip
wget https://query.data.world/s/kjlqbdacff2nhedaegpfzdbxlf65qz -O FY05q4exp.txt.zip
wget https://query.data.world/s/e4eggazzn2zpheq3qsidobmloxubiy -O FY05rev.txt.zip
wget https://query.data.world/s/aifn6o2g3fskjjyuan63c4paw62r6u -O FY06q1exp.txt.zip
wget https://query.data.world/s/rftfzbd4gpvn32ze7cx22kcgdey4kl -O FY06q2exp.txt.zip
wget https://query.data.world/s/4dcuzinhm2gmjz5nywneubscexdscb -O FY06q3exp.txt.zip
wget https://query.data.world/s/emxoyp2gbqogj2jb56xfn5yhtjbuyh -O FY06q4exp.txt.zip
wget https://query.data.world/s/4bibvn35yk7zoycn66mvntu2v37t6q -O FY06rev.txt.zip
wget https://query.data.world/s/cvwwk3swxunvhtvpgxg7bwhc3tgrmx -O FY07q1exp.txt.zip
wget https://query.data.world/s/sma5ury5f5iv6d2scfpsu2hfwkmwwf -O FY07q2exp.txt.zip
wget https://query.data.world/s/z2xe6dwtyoqryzs4o3apt2meudjemi -O FY07q3exp.txt.zip
wget https://query.data.world/s/xsmod6ecglupopvwkebngx6xhxhycd -O FY07q4exp.txt.zip
wget https://query.data.world/s/muosv734qiy5dmmznyyxbivgoyuurk -O FY07rev.txt.zip
wget https://query.data.world/s/udma27xsabcpuielsttuqwhmtaoajq -O FY08q1exp.txt.zip
wget https://query.data.world/s/vminrwbvgtnjphawz4icwzehwpxo5d -O FY08q2exp.txt.zip
wget https://query.data.world/s/pajryo7wozu35wwjjqu4jc32dwismt -O FY08q3exp.txt.zip
wget https://query.data.world/s/hit67zjrj2tiqkjhyumdk4lbftm373 -O FY08q4exp.txt.zip
wget https://query.data.world/s/lfeoodqv62k5banyhjtn67n26hdf7f -O FY08rev.txt.zip
wget https://query.data.world/s/ojwn2vquobcemu76bog7vuwobswe34 -O FY09q1exp.txt.zip
wget https://query.data.world/s/yvu3oojmtzyhpscm4qnojir2sfhh2l -O FY09q2exp.txt.zip
wget https://query.data.world/s/5aqkagmxoghtjmg3a5hsu2nelh3fvx -O FY09q3exp.txt.zip
wget https://query.data.world/s/xonbejckawm6deb6iebwyvgn6k27lv -O FY09q4exp.txt.zip
wget https://query.data.world/s/uun5x5nk2jcd2quvrb45sfpc7eq4mi -O FY09rev.txt.zip
wget https://query.data.world/s/bmqrwiyosw52fxax2vdlzzxlnaccdk -O FY10q1exp.txt.zip
wget https://query.data.world/s/xa7yhsiu7cunmlhboaneaifxmsy3zy -O FY10q2exp.txt.zip
wget https://query.data.world/s/7iaa5dxnawxiovfpnraxsfw2q76chg -O FY10q3exp.txt.zip
wget https://query.data.world/s/uvpuo6qk43agqwt77zzxhqvktygccv -O FY10q4exp.txt.zip
wget https://query.data.world/s/hgiyg44xm6zp7bywvm6s3zyejkvpfk -O FY10rev.txt.zip

# Data Set 2

wget https://query.data.world/s/2yee5of4losllkhd4k2kvsleozqcdh -O FY11q1exp.txt.zip
wget https://query.data.world/s/bmicjkd5xavjk7xvcczxbepofqkcxn -O FY11q2exp.txt.zip
wget https://query.data.world/s/ywzh5lhnjz5a6prn4d77pwpzkjorcs -O FY11q3exp.txt.zip
wget https://query.data.world/s/3nekkqfci756dc4jx654awa67jedtg -O FY11q4exp.txt.zip
wget https://query.data.world/s/ixfhii3knw2ph76kv3zgginpju5gd7 -O FY11rev.txt.zip
wget https://query.data.world/s/3tmp5ow5gmc3a5vimf6b3cmsotmhob -O FY12q1exp.txt.zip
wget https://query.data.world/s/b5mzpekwfyaj65f2l2yanhdfwwyt2b -O FY12q2exp.txt.zip
wget https://query.data.world/s/wpzerhhlqjuhbcy2dgd76djxb3lxfh -O FY12q3exp.txt.zip
wget https://query.data.world/s/l46lu364uti5wt5xghsjtfp75qwkmd -O FY12q4exp.txt.zip
wget https://query.data.world/s/bi5khx73avzgivizrusnfpatuy2uew -O FY12rev.txt.zip
wget https://query.data.world/s/khs45ljjrgvyfjpywhxamwqhvtnio6 -O FY13q1exp.txt.zip
wget https://query.data.world/s/qs2otx4gxv2ku7tjkzv6rj4m2wm7c3 -O FY13q2exp.txt.zip
wget https://query.data.world/s/q2ye3mul5i46ktwjaisd5maicekags -O FY13q3exp.txt.zip
wget https://query.data.world/s/bccloqmly2jy4tjwjmze6mtvfu7bns -O FY13q4exp.txt.zip
wget https://query.data.world/s/uvewtkful2gvaltsvzrldh7tdxhu76 -O FY13rev.txt.zip
wget https://query.data.world/s/tpmiajyfd3raojpvesc75iicxkmand -O FY14q1exp.csv.zip
wget https://query.data.world/s/w2jcpszrvziqiqg6aa2w75qiuu2way -O FY14q2exp.csv.zip
wget https://query.data.world/s/ys37zzhbkfuyayiffba4rzd6y7tm3i -O FY14q3exp.csv.zip
wget https://query.data.world/s/3dc3qokag7j6r25ghdhc4w6bduyx7n -O FY14q4exp.csv.zip
wget https://query.data.world/s/vaistrrj45lnpbu3l2hwld6rqeo3gs -O FY14rev.csv.zip
wget https://query.data.world/s/nktyebqvonwoenvdiuh7k3tbwuz2xw -O FY15q1exp.csv.zip
wget https://query.data.world/s/6b5pnabd4jdjsap242jftdidjfajyg -O FY15q2exp.csv.zip
wget https://query.data.world/s/cmirnpkqak5dhiq5jooliv7krei6bk -O FY15q3exp.csv.zip
wget https://query.data.world/s/zcfxgm3igobjav6cf24fhvfxqti4aj -O FY15q4exp.csv.zip
wget https://query.data.world/s/qvvttpijjlnnfaxics6tctfftpmw7c -O FY15rev.csv.zip
wget https://query.data.world/s/k3wdxhcssk5ylde67urfb53ar6ic5a -O FY16q1exp.csv.zip
wget https://query.data.world/s/zncvbo22cxw5mxsdawloxi4cegtdog -O FY16q2exp.csv.zip
wget https://query.data.world/s/kwu356x42i3lakqojagrlyaqaukrhz -O FY16q3exp.csv.zip
wget https://query.data.world/s/znuympwzxrnqo565hovrliktnmaimc -O FY16q4exp.csv.zip
wget https://query.data.world/s/sm7vjdw7be255kmeybnm4tojckptxz -O FY16rev.csv.zip

for z in *.zip; do unzip $z; done

#############################################################################################
# Common Data
#############################################################################################

cd ..

mkdir common
cd common

wget https://query.data.world/s/cxztnf6c5thufojl3z4ksky2ae2dni -O aRefAgencyCSV.txt
wget https://query.data.world/s/eyq63ii7gpntoyauzf6aqj7fo4womc -O aRefAgencyTab.txt
wget https://query.data.world/s/yoafm6pp3ul3crwetyccpyqasdmqrz -O aRefFundCSV.txt
wget https://query.data.world/s/2tt42ysblefkbqzujztcemwsfeb5b5 -O aRefObjectCSV.txt
wget https://query.data.world/s/4gmbrbajhvub54lbft7742uypxmxmj -O aRefProgramCSV.txt
wget https://query.data.world/s/u6ozp2kse2oiprenwal22y3jeecpef -O aRefSourceCSV.txt
wget https://query.data.world/s/dma3247ttg64sbr3jxukyxxvqtsvnd -O data-dictionary.xlsx
wget https://query.data.world/s/oop3wmsmyw4amk3hdg24tbyxfe657a -O README.md
wget https://query.data.world/s/id47iav2f5rtkqyrfofophdckjxrny -O StateContractList.xlsx

#############################################################################################
# PO Data
#############################################################################################

cd ..

mkdir poData
cd poData

# Dataset 1

wget https://query.data.world/s/2jcsub6it27mv5gcx2wtpm2m6lpk6d -O VA_opendata_FY2001.txt.zip
wget https://query.data.world/s/rc6rxj5kslz3oy4wh2qaysry7mfxph -O VA_opendata_FY2002.txt.zip
wget https://query.data.world/s/7bnjeux7fmnwps4gtxxy4uertnpayx -O VA_opendata_FY2003.txt.zip
wget https://query.data.world/s/qcu4ztkf4qfzqkqqbhq7uzruxeo45r -O VA_opendata_FY2004.txt.zip
wget https://query.data.world/s/3pwlzz6xhkqoeluua723x6acfbk4c4 -O VA_opendata_FY2005.txt.zip
wget https://query.data.world/s/fltp542a6rzoa72a7uhtevkblxdbrr -O VA_opendata_FY2006.txt.zip
wget https://query.data.world/s/fnkd4zoo47x4mlvuudh23acsfje3ea -O VA_opendata_FY2007.txt.zip
wget https://query.data.world/s/nvcpxp356vbo3v643spf4nfkqay2ey -O VA_opendata_FY2008.txt.zip
# Missing 2009
wget https://query.data.world/s/cw7snky4wtufl6y73jghuqztivkfco -O VA_opendata_FY2010.txt.zip

# Dataset 2

wget https://query.data.world/s/57yzgdpdantzvtjd3u7jziwekvpimm -O VA_opendata_FY2011.txt.zip
wget https://query.data.world/s/eyabtvfhpfiiz5h24rotytlzpghzg7 -O VA_opendata_FY2012.txt.zip
wget https://query.data.world/s/rnotz5hjhsul7kbznvapnzxqpbpldn -O VA_opendata_FY2013.txt.zip
wget https://query.data.world/s/mk4ptjrc3iwoaco5zac5snv2p4f2wu -O VA_opendata_FY2014.txt.zip

# Dataset 3

wget https://query.data.world/s/4wxl44lod7diufeom7dcfepcu2bq54 -O VA_opendata_FY2015.txt.zip
wget https://query.data.world/s/qwox3f2242ujqtru7i3sx3rqz4uukj -O VA_opendata_FY2016.txt.zip

# 2017 and 2018 not downloaded

for z in *.zip; do unzip $z; done