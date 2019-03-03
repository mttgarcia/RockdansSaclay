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
from ecdsa import SigningKey, NIST224p

def set_pin():
    #Génération du PIN
    pin = []
    for i in range(4):
        alea = randint(0,9)
        pin.append(str(alea))
    #On passe le tableau en string
    pin = ''.join(pin)
    print ("Le pin de cette carte est le : ",pin)
    #On sépare le pin en 2 pour le passer a la carte
    pin1 = hex(int(pin[0]+pin[1])).replace('0x','')
    pin2 = hex(int(pin[2]+pin[3])).replace('0x','')
    #Si on avait eu en aléatoire un 07, le int le passe en 7 on remet donc le zero
    if (len(pin1)<2):
            pin1 = '0'+pin1
    if (len(pin2)<2):
            pin2 = '0'+pin2
    return pin1, pin2

def set_prenom():
    #La taille du prénom définie dans la carte
    taille_name = 24
    while (True):
        #Saisie du prenom
        prenom = input("Entrez votre prénom : ")
        #Erreur si les caractères ne sont pas cohérents
        if not re.match("^[a-z,A-Z]*$", prenom):
            print ("Erreur, seules les lettres de a à z sont tolérées!")
        #Si le nom est trop grand on recommence
        elif len(prenom) > 12:
            print ("Erreur, maximum 12 charactères!")
        else :
            break
    #Conversion en hexa
    prenom = prenom.encode().hex()
    #Bourrage si le nom est trop petit
    if (len(prenom)<taille_name):
            for i in range(taille_name-len(prenom)):
                    prenom = prenom + '0'
    return prenom

def set_nom():
    #La taille du nom définie dans la carte
    taille_name = 24
    nom = input("Entrez votre nom : ")
    while (True):
        #Erreur si les caractères ne sont pas cohérents
        if not re.match("^[a-z,A-Z]*$", nom):
            print ("Erreur, seules les lettres de a à z sont tolérées!")
        #Erreur si le nom est trop grand
        elif len(nom) > 12:
            print ("Erreur, maximum 12 charactères!")
        else :
            break
    #Conversion en hexa
    nom = nom.encode().hex()
    #Bourrage si le nom est trop petit
    if (len(nom)<taille_name):
            for i in range(taille_name-len(nom)):
                    nom = nom + '0'
    return nom

def set_numpart():
    #Taille du numéro participant définie dans la carte
    taille_numpart = 10
    #On récupère le dernier numéro participant +1 en lecture
    r = open("Client/num_participant.txt","r")
    num_part = r.read()
    print("Numéro participant : ",num_part)
    #On ferme le fichier
    r.close()
    #On le réouvre en écriture pour ajouter 1 pour le prochain numéro participant
    w = open("Client/num_participant.txt","w")
    compteur = int(num_part)+1
    w.write(str(compteur))
    w.close()
    #On le met en hexa
    num_part = hex(int(num_part)).replace('0x','')
    #Si il est trop petit on fait du bourrage
    if (len(num_part)<taille_numpart):
            for i in range(taille_numpart-len(num_part)):
                    num_part = num_part+'0'
    return num_part

def set_sk():
    #On génére un clé privée pour la carte
    sk = SigningKey.generate(curve=NIST224p)
    #On la passe en hexa
    sk_hex = sk.to_string().hex()
    return sk_hex

def set_signature(num_part):
    #On récupère la clé privée du TPE
    sk_client = SigningKey.from_pem(open("Client/private.pem").read())
    #On signe le numéro participant
    signature = sk_client.sign(num_part.encode())
    #On passe la signature en hexa
    signature_hex = signature.hex()
    return signature_hex


pin1,pin2 = set_pin()
nom = set_nom()
prenom = set_prenom()
num_part = set_numpart()
signature = set_signature(num_part)
sk = set_sk()
#On définie la commande d'installation
command = 'java -jar /home/grs/JavaCard/GlobalPlatformPro/gp.jar --install Participant221.cap --params '+pin1+pin2+nom+prenom+num_part+signature+sk

#Lancement de l'installation 
os.system(command)
