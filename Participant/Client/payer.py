#################################
###                           ###
###         PYTHON 3          ###
###                           ###
#################################

# -*- coding: utf-8 -*

from smartcard.System import readers
from smartcard.Exceptions import *
import sys
import time
from ecdsa import VerifyingKey, BadSignatureError, SigningKey
import datetime

global PIN
PIN = False
global AUTH
AUTH = False

def connexion():
    #Tant que la carte n'a pas été inséré
    while (True):
        try :
            r=readers()
            connection=r[0].createConnection()
            connection.connect()
            #Selection AID
            data, sw1, sw2 = connection.transmit([0x00,0xA4,0x04,0x00,0x08,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08])
            if ((int(sw1)!=144) or (int(sw2)!=0)):
                print("Probleme de communication")
                sys.exit()
            if((int(sw1)==144) and (int(sw2)==0)) :
                    print("Connexion etablie")
            if(AUTH == False):
                authentification(connection)
            break
        except NoCardException:
            print("Veuillez insérer la carte")
            time.sleep(5)
        except CardConnectionException:
            print("La carte a été retiré")
            print("Veuillez insérer la carte")
    return connection

def entrer_pin(connection):
    global PIN
    try:
        print("Entrez le code pin :")
        while(True):
            try:
                pin=input()
                print('')
                #On découpe le code pin en deux pour vérifier sur la carte
                pin1 = int(pin[0]+pin[1])
                pin2 = int(pin[2]+pin[3])
                break
            except IndexError:
                print("Le code PIN inséré est vide")
                print("Veuillez entrer le code pin ou quitter :")
        data, sw1, sw2 = connection.transmit([0xB0,0x00,0x00,0x00,0x02,pin1,pin2])
        #Si le code Pin est incorrecte 
        #On prévient et on sort
        if((int(sw1)==99) and (int(sw2)==3)):
                print("Pin incorrecte")
                sys.exit()
        #Si le code Pin est correct on continue
        if((int(sw1)==144) and (int(sw2)==0)) :
                print("Pin correcte")
                PIN = True
    except CardConnectionException:
            print("La carte a été retiré")
            sys.exit()

def afficher_credit(connection):
    try :
        if (PIN == False):
            entrer_pin(connection)
        #Affichage du credit
        print("Voici le credit restant :")
        data, sw1, sw2 = connection.transmit([0xB0,0x01,0x00,0x00,0x00])
        #On récupére le crédit en deux byte, on le reforme en int
        credit = (data[0]<<8)+data[1]
        print (credit)
    except CardConnectionException:
            print("La carte a été retiré")
            sys.exit()

def payer(connection):
    try:
        if (PIN == False) :
            entrer_pin(connection)
        print("Entrez le montant a debiter")
        debit = int(input())
        if(debit>255) :
                print("Le debit doit être inferrieur à 255")
        else :
            data, sw1, sw2 = connection.transmit([0xB0,0x2,0x00,0x00,0x00,debit])
            if((int(sw1)==99) and (int(sw2)==1)) :
                    print("Solde insufissant")
            log(connection,debit)
            print("Vous avez été débité de",debit)
            return debit, connection
    except CardConnectionException:
        print("La carte a été retiré")
        sys.exit()

def crediter(connection):
    global PIN
    try:
        debit, connection = payer(connection)
        #On deconecte la carte, et on laisse 3s pour retirer la carte
        connection.disconnect()
        PIN = False
        print("Retirer la carte")
        time.sleep(3)
        #Puis on laisse 10s pour insérer la seconde carte 
        print("Entrer la carte a crediter")
        time.sleep(10)
        connection= connexion()
        data, sw1, sw2 = connection.transmit([0xB0,0x03,0x00,0x00,0x00,debit])
        print ("Le transfert à été effectué")
        return connection
    except CardConnectionException:
        print("La carte a été retiré")
        sys.exit() 

def authentification(connection):
    global AUTH
    #On récupère la clé publique dans le fichier publib.pem
    vk = VerifyingKey.from_pem(open("Client/public.pem").read())
    data, sw1, sw2 = connection.transmit([0xB0,0x04,0x00,0x00,0x00])
    sig = ''
    num_part = ''
    cpt = 0
    for i in data:
        #Les 56 premieres cases du tab data correspondent à la signature
        if cpt<56:
            s = hex(i)
            #Si il nous retourne un chiffre<16 il faut ajouter un zero avant
            #Par soucis de cohérence car on retire tout les 0x à la ligne 108
            #exemple : si on a 0xc on modifie pour avoir 0x0c
            #Si on avait 0xc0x7f on obtient après le replace('Ox','') c7f
            #Alors que sur la signature de la carte on a 0c7f
            if(len(s)<4):
                s = '0' + s
            sig = sig+s
        #Le reste correspond au numéro participant
        else :
            s = hex(i)
            #Même chose que sur la ligne 91 
            if(len(s)<4):
                s = '0' + s
            num_part = num_part + s
        cpt += 1
    #Le famaux replace pour retirer la 0x
    sig = sig.replace('0x','')
    #On repasse la signature en byte pour le verify
    sig = bytes.fromhex(sig)
    #De repace le numéro participant en byte pour le verify
    num_part = num_part.replace('0x','').encode()
    try:
        #Si il retourne vrai on l'envoie à connexion
        vk.verify(sig, num_part)
        print("Carte authentifié !")
        AUTH = True
        return 
    #Si il retourne l'exception mauvaise signature on prévient connexion
    except BadSignatureError:
        print ("Carte falsifié !")
        print ("Veuillez prevenir les autorités compétentes")
        sys.exit()

def log(connection,debit):
    now = datetime.datetime.now()
    data, sw1, sw2 = connection.transmit([0xB0,0x05,0x00,0x00,0x00])
    nom = ''
    prenom=''
    num_part=''
    date = now.strftime("%Y-%m-%d %H:%M")
    cpt = 0
    for i in data:
        if (i != 0) :
            if (cpt<12):
                nom = nom + chr(i)
            elif (cpt<24):
                prenom = prenom + chr(i)
            else :
                num_part = num_part + str(i)
        cpt += 1
    fichier = open("log.txt","a")
    logs = nom +' ' + prenom+ ' ' + num_part +' ' + str(debit) + 'credit ' + date + '\n'
    fichier.write(logs)
    fichier.close()
    return

connection = connexion()

while(True) :
        print("1 - Voir le credit")
        print("2 - Payer")
        print("3 - Crediter")
        print("4 - Quitter")
        choix = input()
        print('')

        if (int(choix) == 1) :
                afficher_credit(connection)
                print('')
        if (int(choix) == 2) :
                payer(connection)
                print('')
        if (int(choix) == 3) :
                connection = crediter(connection)
                print('')
        if(int(choix) == 4) :
                connection.disconnect()
                sys.exit()