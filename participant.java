package participant;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;

public class Participant extends Applet {

        public static final byte CLA_MONAPPLET = (byte) 0xB0;

        public static final byte INS_GET_NOM = 0x00;
        public static final byte INS_GET_PRENOM = 0x01;
        public static final byte INS_GET_CREDIT = 0x02;
        public static final byte INS_GET_NUMPARTICIPANT = 0x03;

        public static final byte INS_SET_NOM = 0x10;
        public static final byte INS_SET_PRENOM = 0x20;
        public static final byte INS_SET_NUMPARTICIPANT = 0x30;


        private static final byte NOM_LENGHT = 0x10;
        private static final byte NUMPARTICIPANT_LENGHT = 0x08;

        /* Attributs */
        private byte credit;
        private byte [] nom;
        private byte [] prenom;
        private byte [] numparticipant;


        /* Constructeur */
        private Participant() {
               credit = 500;
               nom = new byte[(short)NOM_LENGHT];
               prenom = new byte[(short)NOM_LENGHT];
               numparticipant = new byte[(short)NUMPARTICIPANT_LENGHT];
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
                    buffer[0] = nom;
                    apdu.setOutgoingAndSend((short) 0, (short) len(nom));
                    break;

            case INS_GET_PRENOM:
                    buffer[0] = prenom;
                    apdu.setOutgoingAndSend((short) 0, (short) len(prenom));;
                    break;

            case INS_GET_CREDIT:
                    buffer[0] = credit;
                    apdu.setOutgoingAndSend((short) 0, (short) 1);
                    break;

            case INS_GET_NUMPARTICIPANT:
                    buffer[0] = numparticipant;
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
            default:
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }
}
