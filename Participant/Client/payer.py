from smartcard.System import readers
import sys
import time
def connexion():
        r=readers()
        connection=r[0].createConnection()
        connection.connect()
        #Selection AID
        data, sw1, sw2 = connection.transmit([0x00,0xA4,0x04,0x00,0x08,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08])
        #print(hex(sw1),hex(sw2))
        if ((int(sw1)!=144) or (int(sw2)!=0)):
            print("Probleme de communication")
            sys.exit()
        if((int(sw1)==144) and (int(sw2)==0)) :
                print("Connexion etablie")
        #Entree du code pin
        print("Entrez le code pin :")
        pin=input()
        pin1 = int(pin[0]+pin[1])
        pin2 = int(pin[2]+pin[3])

        data, sw1, sw2 = connection.transmit([0xB0,0x00,0x00,0x00,0x02,pin1,pin2])
        #print(hex(sw1),hex(sw2))

        if((int(sw1)==99) and (int(sw2)==3)):
                print("Pin incorrecte")
                sys.exit()
        if((int(sw1)==144) and (int(sw2)==0)) :
                print("Pin correcte")
        return connection

def afficher_credit(connection):
        #Affichage du credit
        print("Voici le credit restant :")
        data, sw1, sw2 = connection.transmit([0xB0,0x01,0x00,0x00,0x00])
        #print(hex(sw1),hex(sw2))
        credit = (data[0]<<8)+data[1]
        print (credit)

def payer(connection):
        print("Entrez le montant a debiter")
        debit = int(input())
        if(debit>255) :
                print("Le debit doit etre inferrieur a 255")
        else :
                data, sw1, sw2 = connection.transmit([0xB0,0x2,0x00,0x00,0x00,debit])
                #print(hex(sw1),hex(sw2))
                if((int(sw1)==99) and (int(sw2)==1)) :
                        print("Solde insufissant")
                afficher_credit(connection)
        return debit

def crediter(connection):
        debit = payer(connection)
        connection.disconnect()
        print("Retirer la carte")
        time.sleep(3)
        print("Entrer la carte a crediter")
        time.sleep(10)
        connection2= connexion()
        data, sw1, sw2 = connection2.transmit([0xB0,0x3,0x00,0x00,0x00,debit])
        afficher_credit(connection2)

connection = connexion()

while(True) :
        print("1 - Voir le credit")
        print("2 - Payer")
        print("3 - Crediter")
        print("4 - Quitter")
        choix = input()

        if (int(choix) == 1) :
                afficher_credit(connection)

        if (int(choix) == 2) :
                payer(connection)
        if (int(choix) == 3) :
                crediter(connection)
        if(int(choix) == 4) :
                connection.disconnect()
                sys.exit()
