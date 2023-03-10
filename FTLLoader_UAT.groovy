// ********************************************************************
// THIS IS THE New FTLLoader Groovy code for Boomi
// ********************************************************************
// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator
import com.boomi.execution.ExecutionUtil
import org.apache.tools.ant.types.selectors.SelectSelector


// Place directory for multiple files and file name for single file
//String pathFiles = "C:/Users/AY54337/Downloads/FTLLoader/FTL.D230222.T061503"
String pathFiles = "C:/Users/AY54337/Downloads/FTLLoader/clean.txt"
println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()


/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
ExecutionUtil.setDynamicProcessProperty("DPP_TOTAL_LINE", "0", false)
//document.dynamic.userdefined.XXXXXXX
dataContext.addDynamicDocumentPropertyValues(0, "DDP_EOT", "false")


// Place script after this line.
//----------------------------------------------------------------------------------------------------
import java.text.SimpleDateFormat;
import java.util.Vector;
import groovy.transform.Field;
import com.boomi.execution.ExecutionUtil;
/*
 *  Created: Kortelainen ( not sure, maybe Tarvainen too)
 *  Modified by A.Pitkanen on 23.5.2006-31.5.2006 ( search ATP, for last changes )
 *		- fixed indexoutofbounds when flight without passenger
 *		- fixed the handling of too long frequent flyer card numbers
 *		- fixed the handling of frequent flyer card numbers with level information at the end of line
 *		- added frequet flyer card check digit method ( used with too long numbers)
 *		- quick exit in checkPassenger, when index is too high
 *		- check that next flight tag dosen't include Unicode: 0x1
 *		- strip all control characters in writeRow
 *  Ported to Boomi Groovy by J.Manner 2023
 *      - separated cleanFTL from this script
 *      - global variables
 *      - fixed few possible indexoutofbounds cases
 */

 // to make these global variables within Groovy script class

@Field String passenrg = "";
@Field String pnrname = "";
@Field String partner = "";
@Field String memnum = "";
@Field String fqtv = "";
@Field String recloc = "";
@Field String ctype = "";
@Field String eticket = "";
@Field Vector nextf = new Vector();
@Field String carrier ="";
@Field String flight=""
@Field String destination="";
@Field String flightclass="";
@Field String airport="";
@Field String batch="";
@Field String currmm="";
@Field String mm="";
@Field String dd="";
@Field String strTime="";
@Field int numflights = 0;
@Field int totpass = 0;
@Field int errors = 0;
@Field int qustart = 2;
@Field int numpass = 0;
@Field boolean ftlMessage = false;
@Field int kpl = 0;
@Field int curryy;
@Field byte[] bytesout = null;
@Field boolean FTLEnded = false;
@Field int rowNum = 0;

logger = ExecutionUtil.getBaseLogger();

File file = new File("C:/Users/AY54337/Downloads/FTLLoader/result.txt")


for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    ipfile = new BufferedReader(new InputStreamReader(is));
    outData = new StringBuffer();

    ExecutionUtil.setDynamicProcessProperty("DPP_ERROR", "", false)

    createXML()
/*

 */
    fr = new FileWriter(file);
    br = new BufferedWriter(fr);
    br.write(outData.toString())
    br.close();
    fr.close();

    is = new ByteArrayInputStream(outData.toString().getBytes());
    dataContext.storeStream(is, props);
}

private boolean createXML() {
    try {
        boolean isFtlStart = false;
        for (String strRow = "    "; strRow != "Eof" && !isFtlStart;) {
            strRow = readRow();

            if (strRow.length() < 1)
                strRow = "    ";

            if (strRow.length() >= 3) {
                isFtlStart = strRow.substring(0, 3).equalsIgnoreCase("FTL");
            }
            if (!isFtlStart && strRow.length() >= 4) {
                isFtlStart = strRow.substring(1, 4).equalsIgnoreCase("FTL");
            }
        }
        writeRow("<?xml version=\"1.0\"?>");
        writeRow("<FTLMessageRoot>");
        while (!FTLEnded) {
            collectFTL();
            if (ftlMessage) {
                writeRow("    </FTLMessage>");
            }
            ftlMessage = false;
        }
        writeRow("</FTLMessageRoot>");
        logger.info("Flights " + numflights);
        logger.info("Passengers " + totpass);
        logger.info("Errors " + errors);
    } catch (Exception e3) {
        logger.severe("End of Loop:" + e3.getClass().getName() + e3.getMessage());
        ExecutionUtil.setDynamicProcessProperty("DPP_ERROR", e3.getClass().getName() + e3.getMessage(), false)
        return false;
    }
    return true;
}

private String readRow() {
    String strRow = "";
    try {
        strRow = ipfile.readLine();
        if (strRow == null)
            return "Eof";
    } catch (Exception e) {
        return "Eof";
    }
    return strRow;
}

private void collectFTL() {
    String strRow = "    ";
    strRow = readRow();
    rowNum++;

    //new exception handling code CHANGE-3401
    if(!(strRow.indexOf("/")>0)){
        //handle next line
        strRow = readRow();
    }
    createFlight(strRow);
    if (batch.equals("PART1")) {
        strRow = readRow();
        if(strRow.length() > 2) {
            if (strRow.substring(0, 3).equals("CFG"))
                strRow = readRow();
            if (strRow.length() > 3 && strRow.startsWith("STD/")){
                strTime = strRow.substring(4, strRow.length());
            }
            else {
                strTime = "0000";
                strRow = setBookingclassAndDestination(strRow);
            }
        }
        else {
            strTime = "0000";
            strRow = setBookingclassAndDestination(strRow);
        }
    }
    numpass = 0;
    //do not empty field
    //matk = "";
    boolean ftlend = false;
    boolean lastpart = false;
    while (strRow != "Eof" && !ftlend) {

        strRow = readRow();

        if (strRow.length() > 5 && strRow.substring(0, 6).equals("ENDFTL")) {
            strRow = skipHeaders();
            lastpart = true;
        }
        //bypass headers when section is changes
        //this is to prevent unnecessary passenger data to be added
        //ohitetaan otsikot osanvaihdon yhteydessä jotta
        //matkustajatietoihin ei tule ylimääräisiä tietoja
        //6.11.2009 ly
        if (strRow.length() > 5 && strRow.substring(0, 7).equals("ENDPART")) {
            strRow = skipHeaders();
        }

        if ((strRow.length() >= 3 && strRow.startsWith("QU ")) ||
                (strRow.length() > 3 && strRow.substring(1, 4).equals("QU ")) ||
                (strRow.length() > qustart + 2 && strRow.substring(qustart, qustart + 3).equals("QU "))) {
            strRow = skipHeaders();
            //not last part at least in this case
            //lastpart = true;
        }
        if ((strRow.length() >= 3 && strRow.startsWith("QP ")) ||
                (strRow.length() > 3 && strRow.substring(1, 4).equals("QP ")) ||
                (strRow.length() > qustart + 2 && strRow.substring(qustart, qustart + 3).equals("QP "))) {
            strRow = skipHeaders();
            //not last part at least in this case
            //lastpart = true;
        }
        if ((strRow.length() > 2 && strRow.startsWith("FTL")) ||
                (strRow.length() > 3 && strRow.substring(1, 4).equals("FTL")))
            ftlend = true;
        if (strRow.length() < 1) {
            strRow = " ";
        }
        strRow = setBookingclassAndDestination(strRow);

        if (strRow.substring(0, 1).equals("1")) {
            createPassenger();
            passenrg = strRow.trim();
        }
        if (strRow.substring(0, 1).equals("."))
            passenrg = passenrg + " " + strRow.trim();
    }
    if (lastpart) {
        createPassenger();
    }
    else {
        createPassenger(true);
    }
    if (numpass > 0) {
        writeRow("        </ListOfFTLPassenger>");
        if (lastpart) {
            writeRow("        <LastPart>Yes</LastPart>");
            lastpart = false;
        } else {
            writeRow("        <LastPart>No</LastPart>");
        }
    }
    if (strRow == "Eof")
        FTLEnded = true;
}

private String setBookingclassAndDestination(String strRow) {
    boolean lastpart;

    if(strRow.length() == 0)
        return strRow;

    if (strRow.substring(0, 1).equals("-")) {
        createPassenger();
        if (strRow.length() < 7)
            strRow = checkFlightClass(strRow);
        if (strRow.equals("NILPASSENGER")) {
            lastpart = true;
        } else {
            if (strRow.length() > 7) {
                destination = strRow.substring(1, 4);
                flightclass = strRow.substring(strRow.length() - 1, strRow.length());
            }
            else
            {
                flightclass = strRow.substring(strRow.length() - 1, strRow.length());
            }
        }
    }
    return strRow;
}

private String skipHeaders() {
    String strRow = "";
    boolean newftl = false;
    for (newftl = false; strRow != "Eof" && !newftl;) {
        strRow = readRow();
        if ((strRow.length() > 2 && strRow.startsWith("FTL"))
                || (strRow.length() > 3 && strRow.substring(1, 4).equals("FTL")))
            newftl = true;
    }
    return strRow;
}

private void createFlight(String strRow) {
    String[] monthName = [ "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];

    Date datenow = new Date();
    String dateoutrow = "";
    String FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat datesdf = new SimpleDateFormat(FULL_DATE_FORMAT);
    dateoutrow = datesdf.format(datenow);
    curryy = (new Integer(dateoutrow.substring(0, 4))).intValue();
    currmm = dateoutrow.substring(5, 7);
    if (currmm.length() < 2)
        currmm = "0" + currmm;
    String[] collectedFields = strRow.split(" ");
    int slashPosition = strRow.indexOf("/");
    carrier = strRow.substring(0, 2);
    flight = strRow.substring(2, slashPosition);
    dd = strRow.substring(slashPosition + 1, slashPosition + 3);
    String mmx = strRow.substring(slashPosition + 3, slashPosition + 6);
    for (int i = 0; i < 12; i++)
        if (mmx.equals(monthName[i]))
            mmi = (new Integer(i + 1)).toString();

    if (mmi.length() < 2)
        mm = "0" + mmi;
    else
        mm = mmi
    airport = collectedFields[1].trim();
    batch = collectedFields[2].trim();
    if (currmm.compareTo(mm) < 0)
        curryy--;
    passenrg = "";
    flightclass="";
}
private void createPassenger() {
    createPassenger(false);
}

private void createPassenger(boolean mayContinue) {
    if (passenrg == null || passenrg.length() == 0)
        return;

    String[] collectedFields = passenrg.split(" ");
    int passengerStatus = 2;
    try {
        passengerStatus = checkPassenger(collectedFields, mayContinue);
    } catch (Exception e) {
        logger.severe("Checking the passenger failed "+ e.getClass().getName() + e.getMessage());
    }
    if (passengerStatus == 2) {
        passenrg = "";
        errors++;
        return;
    } else {
        if (passengerStatus == 1) {
            passenrg = "";
            return;
        }
    }

    if (numpass == 0) {
        if (!ftlMessage) {
            writeRow("    <FTLMessage>");
            ftlMessage = true;
        }
        try {
            createXMLflight();
        } catch (Exception e) {
            logger.severe("createXMLflight failed");
        }
        writeRow("        <ListOfFTLPassenger>");
    }
    passenrg = "";
    numpass++;
    writeRow("            <FTLPassenger>");
    if (pnrname.length() > 0)
        writeRow("                <PNRName>" + pnrname + "</PNRName>");
    if (recloc.length() > 0)
        writeRow("                <GDSRecordLocator>" + recloc + "</GDSRecordLocator>");
    for (int i = 0; i < nextf.size(); i++)
        writeRow("                <NextFlight>" + (String) nextf.elementAt(i) + "</NextFlight>");

    if (ctype.length() > 0)
        writeRow("                <CheckInType>" + ctype + "</CheckInType>");
    if (eticket.length() > 0)
        writeRow("                <ETicket>" + eticket + "</ETicket>");
    writeRow("                <Partner>" + partner + "</Partner>");
    writeRow("                <MemberNumber>" + memnum + "</MemberNumber>");
    writeRow("                <FTLType>" + fqtv + "</FTLType>");
    writeRow("                <DestinationAirport>" + destination + "</DestinationAirport>");
    writeRow("                <BookingClass>" + flightclass + "</BookingClass>");
    totpass++;
    writeRow("            </FTLPassenger>");
    passenrg = "";
}

/**
 *
 * @param collectedFields
 * @param mayContinue
 * @return int value  0 = ok ; 1 = not ok but may continue ; 2 = not ok, may not continue
 */
private int checkPassenger(String[] collectedFields, boolean mayContinue) {
    nextf.clear();
    ctype = "";
    pnrname = "";
    partner = "";
    memnum = "";
    fqtv = "";
    recloc = "";
    eticket = "";
    int j = 0;
    int i = 0;
    int k = 0;
    int x = 0;
    boolean ok = true;
    for (i = 0; i < collectedFields.size(); i++) {
        if (collectedFields[i].length() > 0) {
            if (collectedFields[i].substring(0, 1).equals("1")) // passenger
                if (pnrname.length() < 1) {
                    pnrname = collectedFields[i].substring(1, collectedFields[i].length());
                    j++;
                }

            if (collectedFields[i].substring(0, 1).equals(".")
                    && collectedFields[i].length() > 1) {
                //If passinger is traveling co-op flight and marketing carrier is
                //not AY (.M), passenger is bypassed / ly 5.11.2009
                if (collectedFields[i].substring(0,2).equals(".M")) {
                    logger.warning("PARSING info: different marketing carrier - passenger bypassed " + passenrg);
                    ok = false;
                }
                /*
                FQTV - frequent flyer account that the miles should be credited to.
                FQTS - frequent flyer account denoting which status benefits to use.
                FQTR - frequent flyer account that provided miles for a redemption (can be different to that of the Pax flying).
                FQTU - frequent flyer account that provided miles for an upgrade.
                */
                if (collectedFields[i].length() > 5
                        && collectedFields[i].substring(0, 6).equals(".R/FQT")) {
                    if (collectedFields[i].length() < 7)
                        collectedFields[i] = collectedFields[i] + "V";
                    fqtv = collectedFields[i].substring(3, 7);
                    if (collectedFields.size() != i + 1) {
                        if (collectedFields[i + 1].length() > 2) {
                            partner = collectedFields[i + 1].substring(0, 2);
                            memnum = collectedFields[i + 1].substring(2,
                                    collectedFields[i + 1].length());
                            x = 1;
                        } else {
                            if (collectedFields[i + 1].equals("FF")) {
                                partner = collectedFields[i + 2].substring(0, 2);
                                memnum = collectedFields[i + 2].substring(2,
                                        collectedFields[i + 2].length());
                            } else if (collectedFields[i + 1].length() > 1) {
                                partner = collectedFields[i + 1].substring(0, 2);
                                memnum = collectedFields[i + 2];
                            }
                            x = 2;
                        }
                        j++;
                        i += x;
                    }
                    k++;
                    if (memnum.length() > 4
                            && memnum.substring(0, 4).equals("FFAY"))
                        memnum = memnum.substring(4, memnum.length());
                    if (memnum.length() > 4
                            && memnum.substring(0, 4).equals("AYFF"))
                        memnum = memnum.substring(4, memnum.length());
                    if (memnum.length() > 4
                            && memnum.substring(0, 2).equals("AY"))
                        memnum = memnum.substring(2, memnum.length());
                    if (memnum.length() > 4
                            && memnum.substring(0, 2).equals("FF"))
                        memnum = memnum.substring(2, memnum.length());
                }
                if (collectedFields.size() <= i) {
                    logger.warning("PARSING error: Something wrong, index too high " + passenrg);
                    return 2;
                }
                if (collectedFields[i].length() < 5
                        && collectedFields[i].substring(0, 2).equals(".O")) {
                    logger.warning("PARSING error: incorrect next flight, " + passenrg);
                    ok = false;
                }
                if (collectedFields[i].length() > 4
                        && collectedFields[i].substring(0, 2).equals(".O")
                        && !collectedFields[i].substring(3, 4).equals("\001")
                        && !collectedFields[i].substring(3, 4).equals("\002")
                        && !collectedFields[i].substring(3, 4).equals("\003")
                        && collectedFields[i].substring(3, collectedFields[i].length())
                        .indexOf("\001") == -1)
                    nextf.add(collectedFields[i].substring(3, collectedFields[i].length()));
                if (collectedFields[i].length() > 6
                        && collectedFields[i].substring(0, 7).equals(".R/CTYP")
                        && collectedFields[i].length() > 7)
                    ctype = collectedFields[i].substring(7, collectedFields[i].length());
                if (collectedFields[i].length() > 5
                        && collectedFields[i].substring(0, 6).equals(".R/TKN")
                        && collectedFields[i].length() > 6)
                    eticket = collectedFields[i].substring(6, collectedFields[i].length());
                if (collectedFields[i].length() > 2
                        && collectedFields[i].substring(0, 2).equals(".L")) {
                    recloc = collectedFields[i].substring(3, collectedFields[i].length());
                    if (recloc.length() > 6)
                        recloc = recloc.substring(0, 6);
                }
            }
        }
    }

    if (j < 2) {
        logger.warning("PARSING error: message incomplete, "  + passenrg);
        ok = false;
    }
    if (k == 0) {
        logger.warning("PARSING error: FF number missing, "   + passenrg);
        ok = false;
    }
    if (k > 1) {
        logger.warning("PARSING error: several FF numbers, "  + passenrg);
        ok = false;
    }
    //logger.info("check partners");
    if (partner.equals("AY"))
        try {
            Long member = new Long(memnum);
            if (memnum.length() > 9)
                if (isCheckDigitOk(memnum.substring(0, 9))) {
                } else {
                    logger.warning("FF number (number too long):(" + memnum + "), " + passenrg);
                    ok = false;
                }
        } catch (NumberFormatException e) {
            try {
                Long secondChance = new Long(memnum.substring(0, 9));
                memnum = memnum.substring(0, 9);
            } catch (Exception e2) {
                logger.warning("PARSING error: incorrect FF number (not a number):(" + memnum + "), " + passenrg);
                ok = false;
            }
        }
    if (partner.length() < 2) {
        logger.warning("PARSING error: incorrect partner:("                + partner + "), " + passenrg);
        ok = false;
    }
    if (!is8bit(pnrname)) {
        logger.warning("PARSING error: incorrect PNRName :("                + pnrname + "), " + passenrg);
        ok = false;
    }
    if (!is8bit(memnum)) {
        logger.warning(                "PARSING error: incorrect member number:(" + memnum + "), "                        + passenrg);
        ok = false;
    }
    if (!is8bit(flight)) {
        logger.warning(                "PARSING error: incorrect FlightNumber:(" + flight + "), "                        + passenrg);
        ok = false;
    }
    if (!is8bit(airport)) {
        logger.warning("PARSING error: incorrect FTL Airport:("                + airport + "), " + passenrg);
        ok = false;
    }
    if (!is8bit(carrier)) {
        logger.warning("PARSING error: incorrect carrier:("                + carrier + "), " + passenrg);
        ok = false;
    }
    if (!is8bit(batch)) {
        logger.warning("PARSING error: incorrect partnumber:("                + batch + "), " + passenrg);
        ok = false;
    }
    if (!is8bit(strTime)) {
        logger.warning(                "PARSING error: incorrect departure time:(" + strTime + "), "                        + passenrg);
        ok = false;
    }
    for (i = 0; i < nextf.size(); i++) {
        if (is8bit((String) nextf.elementAt(i)))
            continue;
        logger.warning("PARSING error: incorrect NextFlight:("                + (String) nextf.elementAt(i) + ")");
        ok = false;
        break;
    }
    if (ok) {
        return 0;
    }
    if (!ok && mayContinue) {
        return 1;
    }
    return 2;
}

private boolean is8bit(String s) {
    int numchars = s.length();
    boolean isutf = true;
    for (int i = 0; i < numchars; i++) {
        int c = s.charAt(i);
        if (c < 0 || c > 127)
            isutf = false;
    }

    return isutf;
}

private void createXMLflight() {
    writeRow("        <PartNumber>" + batch.substring(4, 5)
            + "</PartNumber>");
    writeRow("        <FTLFlight>");
    writeRow("            <Carrier>" + carrier + "</Carrier>");
    writeRow("            <FlightNumber>" + flight + "</FlightNumber>");
    writeRow("            <FlightDate>"
            + (new Integer(curryy)).toString() + "-" + mm + "-" + dd
            + "</FlightDate>");
    writeRow("            <FTLAirport>" + airport + "</FTLAirport>");
    if (strTime.equals(""))
        strTime="0000";
    writeRow("            <DepartureTime>" + strTime + "</DepartureTime>");
    writeRow("        </FTLFlight>");
    numflights++;
}

private void writeRow(String strRow) {
    String outstrRow = "";
    try {
        outstrRow = strRow.replaceAll("\\p{Cntrl}", "") + "\r" + "\n";
        outData.append(outstrRow);
    } catch (IOException ioexception) {
    }
}


private String checkFlightClass(String ipstrRow) {
    String local_strRow = "";
    int BUFFER_SIZE = 200;
    try {
        int len = 0;
        ipfile.mark(BUFFER_SIZE);
        local_strRow = readRow();
        if (local_strRow == "Eof")
            return local_strRow;
        int pos = local_strRow.indexOf("/1");
        if (pos == -1 && local_strRow.equals("NIL") && ipstrRow.length() < 5) {
            pos = 0;
            local_strRow = "NILPASSENGER";
        } else {
            if (pos < 0) {
                //bypassing passenger rows until the last and claim strRow to be a "NILPASSENGER"
                while (local_strRow.startsWith("1")) {
                    ipfile.mark(BUFFER_SIZE);
                    local_strRow = readRow();
                }
                pos = -1;
                local_strRow = "NILPASSENGER";
            }
            else {
                local_strRow = ipstrRow + local_strRow.substring(0, pos);
            }
        }
        ipfile.reset();
    } catch (Exception e) {
        return "Eof";
    }
    return local_strRow;
}

public static String checkdigit(String id) {
    char[] digits = id.toCharArray();
    String csum = (200
            - (digits[0] - 48)
            - (digits[2] - 48)
            - (digits[4] - 48)
            - (digits[6] - 48)
            - (int) Math
            .floor(2.2000000000000002D * (double) (digits[1] - 48))
            - (int) Math
            .floor(2.2000000000000002D * (double) (digits[3] - 48))
            - (int) Math
            .floor(2.2000000000000002D * (double) (digits[5] - 48)) - (int) Math
            .floor(2.2000000000000002D * (double) (digits[7] - 48)))
    + "";
    return csum.substring(csum.length() - 1);
}

public static boolean isCheckDigitOk(String fqtvnr) {
    String checkDigit = checkdigit(fqtvnr.substring(0, 8));
    return checkDigit.equals(fqtvnr.substring(8, 9));
}

