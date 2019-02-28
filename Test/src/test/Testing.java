package test;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

public class Testing extends Applet {

	public static final byte CLA_MONAPPLET = (byte) 0xB0;

	public static final byte INS_GET_CREDIT = 0x02;

	private byte credit;

	private Testing() {
        credit = 50;
    }

    public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
        new Testing().register();
    }

	public void process(APDU apdu) throws ISOException {
		byte[] buffer = apdu.getBuffer();

		if (this.selectingApplet()) return;

		if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

        switch (buffer[ISO7816.OFFSET_INS]) {

        	case INS_GET_CREDIT:
        		buffer[0] = credit;
				apdu.setOutgoingAndSend((short) 0, (short) 1);
				break;

			default:
					ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}
