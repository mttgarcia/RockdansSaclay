#################################
###                           ###
###         PYTHON 3          ###
###                           ###
#################################

# -*- coding: utf-8 -*

from random import *
import os
import re
import sys
import codecs
from ecdsa import SigningKey, NIST256p

#Taille prédéfinie
taille_name = 24
taille_numpart = 10

r = open("Client/num_participant.txt","r")
num_part = r.read()
r.close()
w = open("Client/num_participant.txt","w")
compteur = int(num_part)+1
if (len(num_part)<taille_numpart):
        for i in range(taille_numpart-len(num_part)):
                num_part = num_part+'0'

w.write(str(compteur))
r.close()
w.close()

#Génération du PIN
pin = []
for i in range(4):
        alea = randint(0,9)
        pin.append(str(alea))

pin = ''.join(pin)
print ("Le pin de cette carte est le :",pin)
pin1 = hex(int(pin[0]+pin[1])).replace('0x','')
pin2 = hex(int(pin[2]+pin[3])).replace('0x','')
if (len(pin1)<2):
        pin1 = '0'+pin1
if (len(pin2)<2):
        pin2 = '0'+pin2

#Saisie du nom
nom = input("Entrez votre nom : ")
if not re.match("^[a-z,A-Z]*$", nom):
    print ("Erreur, seules les lettres de a à z sont tolérées!")
    sys.exit()
elif len(prenom) > 12:
    print ("Erreur, maximum 12 charactères!")
    sys.exit()

#print ("Le prenom enregistré est : ", prenom)

#Conversion en hexa
nom = nom.encode().hex()
#print ("Hexa : ", nom)
if (len(nom)<taille_name):
        for i in range(taille_name-len(nom)):
                nom = nom + '0'
#print(nom)
        
#Saisie du prenom
prenom = input("Entrez votre prenom : ")
if not re.match("^[a-z,A-Z]*$", prenom):
    print ("Erreur, seules les lettres de a à z sont tolérées!")
    sys.exit()
elif len(prenom) > 12:
    print ("Erreur, maximum 12 charactères!")
    sys.exit()

#print ("Le prenom enregistré est : ", prenom)

#Conversion en hexa
prenom = prenom.encode().hex()
if (len(prenom)<taille_name):
        for i in range(taille_name-len(prenom)):
                prenom = prenom + '0'
sk = SigningKey.generate(curve=NIST256p)
sk_string = sk.to_string()
vk = sk.get_verifying_key()
signature = sk.sign(b(num_part)).hex()

#print ("Hexa : ", prenom)
command = 'java -jar /home/grs/JavaCard/GlobalPlatformPro/gp.jar -v --install Participant222.cap --params '+pin1+pin2+nom+prenom+num_part+signature+sk_string
#print(command)
#Lancement de l'installation 
os.system(command)