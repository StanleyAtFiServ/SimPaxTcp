package org.fiserv;

import org.w3c.dom.Element;
public class AppMsg {
    private final static char START = 'R';
    private final static String xmlPreamble = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private final static String DATA_ACKNOWLEDGE = "<EFTAcknowledgement><AcknowledgementType>0003</AcknowledgementType></EFTAcknowledgement>";
    public final static int MSG_ACK = 1;
    public final static int MSG_RTV = 2;
    public final static char OP_QST = 'Q';
    public final static char OP_ANS = 'A';
    public final static char OP_ACK = 'K';
    public final static char OP_NAK = 'N';

    private int msgType;
    private char opType;
    private String referString;

    public AppMsg( int _msgType, char _opType, String _referString )
    {
        msgType = _msgType;
        opType = _opType;
        referString = _referString;
    }

    public String packMessage()
    {
        String msgOut;
        switch (msgType) {
            case MSG_ACK:
                msgOut = Character.toString(START) + Character.toString(opType) + len5Digit(xmlPreamble +DATA_ACKNOWLEDGE) + xmlPreamble + DATA_ACKNOWLEDGE;
                break;
            case MSG_RTV:
                String tmpMsg;

                XmlTag xmlTag = new XmlTag(referString);
                Element a = xmlTag.readTag("Amount");
                msgOut = null;

//               msgOut = Character.toString(START) + Character.toString(opType) + len5Digit(xmlPreamble + tmpMsg) + xmlPreamble + tmpMsg;
                break;
            default:
                msgOut = null;
                break;
        }
        return msgOut;
    }
    private String len5Digit( String data )
    {
        return String.format("%05d", data.length());
    }
}
