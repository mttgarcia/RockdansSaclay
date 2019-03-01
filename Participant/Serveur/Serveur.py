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
paiement=input("Saisir le montant à payer : ")
if not re.match("^[0-9]*$", paiement):
    print ("Erreur, les chiffres de 0 à 9 sont tolérés!")
    sys.exit()
elif len(paiement) > 4:
    print ("Erreur, maximum payable de 9999€ !")
    sys.exit()

print("Le montant à payer est de :", paiement, "€")

#Conversion en hexa du montant
taille_paiement = 8

paiement = paiement.encode().hex()
print ("Hexa : ", paiement)
if (len(paiement)<taille_paiement):
        for i in range(taille_paiement-len(paiement)):
                paiement = paiement + '0'
print("Le montant en hexa est de : ", paiement)

#Modification de la sortie en 0x 

#data, sw1, sw2 = connection.transmit([0x00,


#Déconnexion de la carte
connection.disconnect()
