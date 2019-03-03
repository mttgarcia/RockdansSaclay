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
import datetime

def set_pin():
    #Génération du PIN
    pin = []
    for i in range(4):
        alea = randint(0,9)
        pin.append(str(alea))
    #On passe le tableau en string
    pin = ''.join(pin)
    print ("Le pin de cette carte est le :",pin)
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
        prenom = input("Entrez votre prenom : ")
        #Erreur si les caractères ne sont pas cohérents
        if not re.match("^[a-z,A-Z]*$", prenom):
            print ("Erreur, seules les lettres de a à z sont tolérées!")
        #Si le nom est trop grand on recommence
        elif len(prenom) > 12:
            print ("Erreur, maximum 12 charactères!")
        else :
            break
    #Conversion en hexa
    prenom_hex = prenom.encode().hex()
    #Bourrage si le nom est trop petit
    if (len(prenom_hex)<taille_name):
            for i in range(taille_name-len(prenom_hex)):
                    prenom_hex = prenom_hex + '0'
    return prenom_hex,prenom

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
    nom_hex = nom.encode().hex()
    #Bourrage si le nom est trop petit
    if (len(nom_hex)<taille_name):
            for i in range(taille_name-len(nom_hex)):
                    nom_hex = nom_hex + '0'
    return nom_hex,nom

def set_numpart():
    #Taille du numéro participant définie dans la carte
    taille_numpart = 10
    #On récupère le dernier numéro participant +1 en lecture
    r = open("Client/num_participant.txt","r")
    num_part = r.read()
    print("Numero participant :",num_part)
    #On ferme le fichier
    r.close()
    #On le rouvre en écriture pour ajouter 1 pour le prochain numéro participant
    w = open("Client/num_participant.txt","w")
    compteur = int(num_part)+1
    w.write(str(compteur))
    w.close()
    #On le met en hexa
    num_part_hex = hex(int(num_part)).replace('0x','')
    #Si il est trop petit on fait du bourrage
    if (len(num_part_hex)<taille_numpart):
            for i in range(taille_numpart-len(num_part_hex)):
                    num_part_hex = num_part_hex+'0'
    return num_part_hex,num_part

def set_sk():
    #On génére un clé privée pour la carte
    sk = SigningKey.generate(curve=NIST224p)
    vk = sk.get_verifying_key()
    #On la passe en hexa
    sk_hex = sk.to_string().hex()
    return sk_hex, str(vk.to_string())

def set_signature(num_part_hex):
    #On récupère la clé privée du TPE
    sk_client = SigningKey.from_pem(open("Client/private.pem").read())
    #On signe le numéro participant
    signature = sk_client.sign(num_part_hex.encode())
    #On passe la signature en hexa
    signature_hex = signature.hex()
    return signature_hex,str(signature)

#Ajoute au log
def log(nom,prenom,num_part,signature,vk):
	fichier = open("logs_part.txt","a")
	date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
	logs = nom +' ' + prenom+ ' ' + num_part + ' ' + signature + ' ' + vk +' ' + date + '\n'
	fichier.write(logs)
	return

pin1,pin2 = set_pin()
nom_hex,nom = set_nom()
prenom_hex,prenom = set_prenom()
num_part_hex,num_part = set_numpart()
signature_hex,signature = set_signature(num_part_hex)
sk_hex,vk = set_sk()
log(nom,prenom,num_part,signature,vk)
#On définie la commande d'installation
command = 'java -jar /home/grs/JavaCard/GlobalPlatformPro/gp.jar --install Participant221.cap --params '+pin1+pin2+nom_hex+prenom_hex+num_part_hex+signature_hex+sk_hex

#Lancement de l'installation 
os.system(command)