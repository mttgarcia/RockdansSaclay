from smartcard.System import readers
r=readers()

#Création de la connexion
connection=r[0].createConnection()

#Connexion à la java card
connection.connect()

#Valeur des variables dans le rocksaclay.java
CLASS_APPLET = 0xB0 	#Valeur de la classe de l'applet
INS_GET_NOM = 0x00 	#Valeur de NOM
INS_GET_PRENOM = 0x01	#Valeur de PRENOM
INS_GET_CREDIT = 0x02	#Valeur de crédit
INS_GET_NUMPARTICIPANT = 0x03	#Valeur du numéro du participant
INS_SET_NOM = 0x10 	#Set du NOM
INS_SET_PRENOM = 0x20	#Set du PRENOM
INS_SET_NUMPARTICIPANT = 0x30	#set du Numéro du participant
INS_SET_CREDIT = 0x40 #set du crédit
NOM_LENGHT = 0x10	#Longueur du NOM
NUMPARTICIPANT_LENGHT = 0x08	#Longeur du numéro du participant


#Selection AID
data, sw1, sw2 = connection.transmit([0x00,0xA4,0x04,0x00,0x08,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x09])
#0x00 CLA
#0xA4 INS
#P1 0x04 
#P2 0x00
#Lc 0x08 Taille de l'AID
#Data Contient l'AID

print (hex(sw1),hex(sw2))

#Fonction qui initialise la valeur de nom
def set_nom(nom):
    n = [CLASS_APPLET, INS_SET_NOM, 0x00, 0x00, len(nom)] + str2array(nom)+[0x00]
    print(n) #On affiche n
    connection.transmit(n) #Envoi le nom à la java card et retourne la réponse

#Fonction qui initialise la valeur de prenom
def set_prenom(prenom):
    p = [CLASS_APPLET, INS_SET_PRENOM, 0x00, 0x00, len(prenom)] + str2array(prenom)+[0x00]
    print(p) #On affiche p
    connection.transmit(p) #Envoi le prenom à la java card et retourne la réponse

#Fonction qui initialise la valeur de Numéro du participant
def set_numpart(numpart):
    nump = [CLASS_APPLET, INS_SET_NUMPARTICIPANT, 0x00, 0x00, len(numpart)] + str2array(numpart)+[0x00]
    print(nump) #On affiche nump
    connection.transmit(nump) #Envoi le numero du participant à la java card et retourne la réponse

#Fonction qui initialise la valeur du credit
def set_credit(credit):
    cred = [CLASS_APPLET, INS_SET_CREDIT, 0x00, 0x00, len(credit)] + str2array(credit)+[0x00]
    print(cred) #On affiche le crédit
    connection.transmit(credit) #Envoi le crédit à la java card et retourne la réponse


#Condition toujours vraie
if __name__ == "__main__":
    print("Entrez votre nom :", set_nom()) 

#Déconnexion de la carte
connection.disconnect()
