#################################
###                           ###
###         PYTHON 3          ###
###                           ###
#################################

# -*- coding: utf-8 -*

from random import *
#from ecdsa import SigningKey, NIST384p
from smartcard.System import readers
import os
import re
import sys
import codecs
rd=readers()

#Création de la connexion
connection=rd[0].createConnection()

#Connexion à la java card
connection.connect()

#Selection AID
data, sw1, sw2 = connection.transmit([0x00,0xA4,0x04,0x00,0x08,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08])
print (hex(sw1),hex(sw2))

#Demande de paiment 
data, sw1, sw2 = connection.transmit([0x00,


#Déconnexion de la carte
connection.disconnect()
