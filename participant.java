package participant;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;

public class Participant extends Applet {

        //Numéro de mon applet
        public static final byte CLA_MONAPPLET = (byte) 0xB0;

        //Les get
        public static final byte INS_GET_NOM = 0x00;
        public static final byte INS_GET_PRENOM = 0x01;
        public static final byte INS_GET_CREDIT = 0x02;
        public static final byte INS_GET_NUMPARTICIPANT = 0x03;

        //Les initialisations
        public static final byte INS_SET_NOM = 0x10;
        public static final byte INS_SET_PRENOM = 0x20;
        public static final byte INS_SET_NUMPARTICIPANT = 0x30;

        //Payer et décrémenter le crédit
        public static final byte INS_PAYER = 0xA0;

        //Les tailles
        private static final byte NOM_LENGHT = 0x10;
        private static final byte NUMPARTICIPANT_LENGHT = 0x08;

        //Exception
        private static final byte SOLDE_INSUFISANT = 0x6302;

        /* Attributs */
        private byte credit;
        private byte [] nom;
        private byte [] prenom;
        private byte [] numparticipant;
        private static final byte compteur = 1;

        /* Constructeur */
        private Participant() {
               credit = 500;
               nom = new byte[(short)NOM_LENGHT];
               prenom = new byte[(short)NOM_LENGHT];
               numparticipant = new byte[(short)NUMPARTICIPANT_LENGHT];
               numparticipant = compteur;
               compteur ++;
        }

        public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
               new ComptApp().register();
        }


public void process(APDU apdu) throws ISOException {
    byte[] buffer = apdu.getBuffer();

    if (this.selectingApplet()) return;

    if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    switch (buffer[ISO7816.OFFSET_INS]) {
            case INS_GET_NOM:
                    Util.arrayCopy(nom, ISO7816.OFFSET_CDATA, buffer, (byte)0, ISO7816.OFFSET_LC); //Prendre ce qu'il y a dans nom, et le mettre dans le buffer
                    apdu.setOutgoingAndSend((short) 0, (short) len(nom));
                    break;

            case INS_GET_PRENOM:
                    Util.arrayCopy(prenom, ISO7816.OFFSET_CDATA, buffer, (byte)0, ISO7816.OFFSET_LC); //Prendre ce qu'il y a dans nom, et le mettre dans le buffer
                    apdu.setOutgoingAndSend((short) 0, (short) len(prenom));;
                    break;

            case INS_GET_CREDIT:
                    buffer[0] = credit;
                    apdu.setOutgoingAndSend((short) 0, (short) 1);
                    break;

            case INS_GET_NUMPARTICIPANT:
                    Util.arrayCopy(numparticipant, ISO7816.OFFSET_CDATA, buffer, (byte)0, ISO7816.OFFSET_LC);
                    apdu.setOutgoingAndSend((short) 0, (short) len(numparticipant));
                    break;

             case INS_SET_NOM:
                    apdu.setIncomingAndReceive();
                    Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, nom, (byte)0, ISO7816.OFFSET_LC);
                    break;

            case INS_SET_PRENOM:
                    apdu.setIncomingAndReceive();
                    Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, prenom, (byte)0, ISO7816.OFFSET_LC);
                    break;

            case INS_SET_NUMPARTICIPANT:
                    apdu.setIncomingAndReceive();
                    Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, numparticipant, (byte)0, ISO7816.OFFSET_LC);
                    break;

            case INS_PAYER:
                apdu.setIncomingAndReceive();
                payer(buffer[ISO7816.OFFSET_CDATA]);

                break;

            default:
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }
}

//Fonction pour payer et décrémenter le crédit
public void payer(byte prix) throws ISOException{
    if (credit >= prix) {
        credit = credit - prix;
        return;
    }
    ISOException.throwIt(SOLDE_INSUFISANT);
    }

//Fonction pour recevoir et incrémenter le crédit
public void recevoir(byte gain) throws ISOException{
    credit = credit + gain;
    return;

}