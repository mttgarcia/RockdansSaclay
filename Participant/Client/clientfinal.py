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

#Taille prédéfinie
taille_name = 24
taille_numpart = 5

r = open("num_participant.txt","r")
num_part = r.read()
r.close()
w = open("num_participant.txt","w")
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
print ("Le pin de cette carte est le : ",pin)

#Saisie du nom
nom = input("Entrez votre nom : ")
if not re.match("^[a-z,A-Z]*$", nom):
    print ("Erreur, seules les lettres de a à z sont tolérées!")
    sys.exit()
elif len(nom) > 12:
    print ("Erreur, maximum 12 charactères!")
    sys.exit()

print ("Le nom enregistré est : ", nom)

#Conversion en hexa
nom = nom.encode().hex()
print ("Hexa : ", nom)
nom = nom.replace("0x",'')
if (len(nom)<taille_name):
	for i in range(taille_name-len(nom)):
		nom = nom + '0'
print(nom)
		
#Saisie du prenom
prenom = input("Entrez votre prenom : ")
if not re.match("^[a-z,A-Z]*$", prenom):
    print ("Erreur, seules les lettres de a à z sont tolérées!")
    sys.exit()
elif len(prenom) > 12:
    print ("Erreur, maximum 12 charactères!")
    sys.exit()

print ("Le prenom enregistré est : ", prenom)

prenom = prenom.encode().hex()
if (len(prenom)<taille_name):
	for i in range(taille_name-len(prenom)):
		prenom = prenom + '0'

print ("Hexa : ", prenom)

#Lancement de l'installation 
os.system('java -jar /home/grs/JavaCard/GlobalPlatformPro/gp.jar -v --install Participant221.cap --params '+pin+nom+prenom+num_part)
