package part;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;
import javacard.security.*

public class Participant extends Applet {

    public static final byte CLA_MONAPPLET = (byte) 0xB0;
    //Instruction
    static final byte INS_CHECK_PIN = 0x00;
    static final byte INS_AFFICHER_CREDIT = 0x01;
    static final byte INS_PAYER = 0x02;
    static final byte INS_CREDITER = 0x03;
    static final byte INS_AUTHENTIFICATION = 0x04;
    //Exception
    static final short SW_SOLDE_INSSUFISANT = 0x6301;
    static final short SW_PIN_VERIFICATION_REQUIRED = 0x6302;
    static final short SW_PIN_VERIFICATION_FAILED = 0x6303;
    //Taille
    private static final byte PIN_LENGTH = 0x02;
    private static final byte NOM_LENGTH = 0x0c;
    private static final byte PRENOM_LENGTH = 0x0c;
    private static final byte NUM_PARTICIPANT_LENGTH = 0x05;
    private static final byte CREDIT_LENGTH = 0x02;
    private static final byte PRIVATE_KEY_LENGTH = 0x1c;
    private static final byte SIGNATURE_CARTE_LENGTH = 0x38;

    //PIN
    private static final byte PIN_TRY_LIMIT = 0x03;

    //DEBUG
    private byte[] MESS_DEBUG = {'D','e','B','U','G',' '};

    //Atribut
    private short credit;
    private static byte[] nom;
    private static byte[] prenom;
    private static byte[] num_participant;
    private static OwnerPIN pin;
    private static byte[] signature_carte;
    private static byte[] private_key;


    private Participant(byte bArray[], short bOffset, byte bLength) {
        //Taille de l'aid
        byte aidLength = bArray[bOffset];
        //Control info (on s'en fout c'est 0 faut le mettre pour faire joli et propre)
        short controlLength = (short)(bArray[(short)(bOffset+1+aidLength)]&(short)0x00FF);
        //Taille des info 
        short dataLength = (short)(bArray[(short)(bOffset+1+aidLength+1+controlLength)]&(short)0x00FF);
        if ((byte)dataLength != (byte)(PIN_LENGTH + NOM_LENGTH + PRENOM_LENGTH + NUM_PARTICIPANT_LENGTH + SIGNATURE_CARTE_LENGTH+ PRIVATE_KEY_LENGTH)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH );
        } 

        //Initialisation du PIN
        pin = new OwnerPIN(PIN_TRY_LIMIT, //Max d'essais de PIN
                                PIN_LENGTH);//Taille du PIN
        pin.update(bArray,//Tab
                (short) (bOffset+1+aidLength+1+controlLength+1),//Offset
                PIN_LENGTH);//Taille du pin

        //Initialisation du nom
        nom = new byte[(short)NOM_LENGTH];
        Util.arrayCopyNonAtomic(bArray,//source
                                    (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH),//offset de source
                                    nom,//dest
                                    (short)0,//offset de dest
                                    NOM_LENGTH);//Taille de la copie

        //Initialisation du prenom
        prenom = new byte[(short)PRENOM_LENGTH];
        Util.arrayCopyNonAtomic(bArray,//source
                                    (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH),//offset de source
                                    prenom,//dest
                                    (short)0,//offset de dest
                                    PRENOM_LENGTH);//Taille de la copie

        //Initialisation du numero participant
        num_participant = new byte[(short)NUM_PARTICIPANT_LENGTH];
        Util.arrayCopyNonAtomic(bArray,//source
                                    (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH+PRENOM_LENGTH),//offset de source
                                    num_participant,//dest
                                    (short)0,//offset de dest
                                    NUM_PARTICIPANT_LENGTH);//Taille de la copie

        //Initialisation de la signature de la carte
        signature_carte = new byte[(short)SIGNATURE_CARTE_LENGTH];
        Util.arrayCopyNonAtomic(bArray,//source
                                    (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH+PRENOM_LENGTH+NUM_PARTICIPANT_LENGTH),//offset de source
                                    signature_carte,//dest
                                    (short)0,//offset de dest
                                    SIGNATURE_CARTE_LENGTH);//Taille de la copie 

        //Initialisation de la clé privée
        private_key = new byte[(short)PRIVATE_KEY_LENGTH];
        Util.arrayCopyNonAtomic(bArray,//source
                                    (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH+PRENOM_LENGTH+NUM_PARTICIPANT_LENGTH+SIGNATURE_CARTE_LENGTH),//offset de source
                                    private_key,//dest
                                    (short)0,//offset de dest
                                    PRIVATE_KEY_LENGTH);//Taille de la copie  

        credit = 500;


    }

    public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
        new Participant(bArray,bOffset,bLength).register();
    }

    public void process(APDU apdu) throws ISOException {

        byte[] buffer = apdu.getBuffer();

        if (this.selectingApplet()) return;

        if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
            switch (buffer[ISO7816.OFFSET_INS]) {

                case INS_CHECK_PIN:
                    //Si le PIN est deja entree on sort
                    if (pin.isValidated()) {
                        return;         
                        }
                    //Si le pin est bon on rentre dans le if
                    if (verify(apdu)) {
                        //On met dans le buffer le nombre d'essais de PIN possible
                        buffer[0] = pin.getTriesRemaining();
                        //On envoie le buffer à l'offset 0 de la taille de 1 
                        apdu.setOutgoingAndSend((short) 0, (short)1);
                        return;
                        }
                    ISOException.throwIt(SW_PIN_VERIFICATION_FAILED);
                    break;

                case INS_AFFICHER_CREDIT:
                    print_credit(apdu);
                    break;

                case INS_PAYER:
                    payer(apdu);
                    break;

                case INS_CREDITER:
                    crediter(apdu);
                    break;

                case INS_AUTHENTIFICATION:
                    authentification(apdu);
                    break;


                default:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                }
        }

    static boolean verify(APDU apdu) throws ISOException {
        //On récupére le buffer du apdu et on le met dans cette variable
        byte[] buffer = apdu.getBuffer();
        //Si le PIN est plus grand que demander on sort une erreur
        if (buffer[ISO7816.OFFSET_LC] != PIN_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH );
        }
        //On récupère ce que le buffer de l'apdu a
        apdu.setIncomingAndReceive(); //Récupere le buffer
        //True si le PIN est valide
        boolean res = pin.check(buffer,//Le pin entré
                                 (short) ISO7816.OFFSET_CDATA, //offset du pin
                                 PIN_LENGTH);//La taille du PIN
        return res; //True or False
        }           

    //Envoie la signature de la carte
    static void authentification(APDU apdu)throws ISOException {
        //On prend le buffer de l'apdu
        byte[] buffer = apdu.getBuffer();
        //Si le pin est valide on rentre dans le if
        if (pin.isValidated()) {
            Util.arrayCopyNonAtomic(signature_carte,(short)0,buffer,(short)0,SIGNATURE_CARTE_LENGTH);
            Util.arrayCopyNonAtomic(num_participant,(short)0,buffer,(short)SIGNATURE_CARTE_LENGTH,NUM_PARTICIPANT_LENGTH);
            //Envoie du buffer à l'offset 0 de la taille du secret
            apdu.setOutgoingAndSend((short) 0, (short)(SIGNATURE_CARTE_LENGTH+NUM_PARTICIPANT_LENGTH));
            return;
            }
        //Si le pin n'est pas mis exception  
        ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

    public void print_credit(APDU apdu) throws ISOException {
        //On prend le buffer de l'apdu
        byte[] buffer = apdu.getBuffer();
        //Si le pin est valide on rentre dans le if
        if (pin.isValidated()) {
            Util.setShort(buffer,(short) 0,credit);
            //Envoie du buffer à l'offset 0 de la taille du secret
            apdu.setOutgoingAndSend((short) 0, CREDIT_LENGTH);
            return;
            }
        //Si le pin n'est pas mis exception  
        ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

    public void payer(APDU apdu) throws ISOException{
        //On prend le buffer de l'apdu
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        //Si le pin n'a pas été validé on sort
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
            }
            short debit = Util.makeShort(buffer[ISO7816.OFFSET_CDATA+1],buffer[ISO7816.OFFSET_CDATA]);
            if(credit > debit){
                credit = (short)(credit - debit);
                //Envoie du buffer à l'offset 0 de la taille du secret
                return;
                }
            else{
                ISOException.throwIt(SW_SOLDE_INSSUFISANT);
                }
            }

    public void crediter(APDU apdu) throws ISOException{
        //On prend le buffer de l'apdu
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        //Si le pin n'a pas été validé on sort
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
            }
        short debit = Util.makeShort(buffer[ISO7816.OFFSET_CDATA+1],buffer[ISO7816.OFFSET_CDATA]);
        credit = (short)(credit + debit);
            }
}
