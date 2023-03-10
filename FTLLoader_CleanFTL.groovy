// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator
import com.boomi.execution.ExecutionUtil
import org.apache.tools.ant.types.selectors.SelectSelector

// Place directory for multiple files and file name for single file
//String pathFiles = "C:/Users/AY54337/Downloads/FTLLoader/FTL.D230306.T061503"
String pathFiles = "C:/Users/AY54337/Downloads/FTLLoader/FTL.D230307.T061503"
//String pathFiles = "C:/Users/AY54337/Downloads/1677103938266.dat"

println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()

/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
//ExecutionUtil.setDynamicProcessProperty("DPP_TOTAL_LINE", "0", false)
//document.dynamic.userdefined.XXXXXXX
//dataContext.addDynamicDocumentPropertyValues(0, "DDP_EOT", "false")
//dataContext.addDynamicDocumentPropertyValues(0, "DDP_CleanedFile", "")



// Place script after this line.
//----------------------------------------------------------------------------------------------------
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

File file = new File("C:/Users/AY54337/Downloads/FTLLoader/clean.txt")

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    outData = new StringBuffer();

    FTL_PASSENGER_LINE_STARTS_WITH_ASCII_CODE_FROM = 49;
    FTL_PASSENGER_LINE_STARTS_WITH_ASCII_CODE_TO = 57;

    reader = new BufferedReader(new InputStreamReader(is));

    outStream = new ByteArrayOutputStream()
    boutStream = new BufferedOutputStream(outStream);

    boolean printNewLines = true;
    int[] lastTwoBytes = new int[2];
    int lineCount = 0;



    while ((orginalByte = (byte) reader.read()) >= 0) {
        orginalInt = (int) orginalByte;
        // remove all but one continuous spaces
        if (lastTwoBytes[0] == 32 && orginalInt == 32) {
            continue;
        }
        // the carriage return (13) is missing after line feed (10) ->
        // add one
        if (orginalInt == 10 && lastTwoBytes[0] != 13) {
            lastTwoBytes[1] = 13;
            lastTwoBytes[0] = 10;
            continue;
        }
        if (lastTwoBytes[0] == 10 && lastTwoBytes[1] == 13) {
            if (FTL_PASSENGER_LINE_STARTS_WITH_ASCII_CODE_FROM <= orginalInt
                    && orginalInt <= FTL_PASSENGER_LINE_STARTS_WITH_ASCII_CODE_TO) {
                boutStream.write(lastTwoBytes[1]);
                boutStream.write(lastTwoBytes[0]);
                printNewLines = false;
            } else if (orginalInt == 32 || orginalInt == 69
                    || orginalInt == 81 || orginalInt == 45
                    || orginalInt == 123 || orginalInt == 125
                    || orginalInt == 127 || orginalInt <= 31) {
                // This is at the end of a passenger part or an end of a
                // bookingclass part, if the first char of a new line
                // is one of:
                // space=32, uppercase E (69), uppercase Q (81), - (45),
                // { (123), } (125), DEL (127), text control char
                boutStream.write(lastTwoBytes[1]);
                boutStream.write(lastTwoBytes[0]);
                printNewLines = true;
                lineCount++;
            } else if (printNewLines) {
                boutStream.write(lastTwoBytes[1]);
                boutStream.write(lastTwoBytes[0]);
                lineCount++;
            } else {
                c = 32
                boutStream.write( c); // replace new line with space
            }
        }
        // add space if not at the end of a line
        if (lastTwoBytes[0] == 32 && orginalInt != 13) {
            boutStream.write(lastTwoBytes[0]);
        }

        // remove most of control characters and other rubbish
        // 10=line feed, 13=carriage return, 2=start of text, 3=end of
        // text, 127=delete, space=32, question mark=63
        if (orginalInt != 32 && orginalInt > 31 && orginalInt < 127
                && orginalInt != 63) {
            boutStream.write(orginalByte);
        }

        lastTwoBytes[1] = lastTwoBytes[0];
        lastTwoBytes[0] = orginalInt;


    }
    boutStream.flush();
    outData.append(outStream.toString())
    props.setProperty("document.dynamic.userdefined.DDP_CLEANFILE", outData.toString());

/*
 */
    fr = new FileWriter(file);
    br = new BufferedWriter(fr);
    br.write(outStream.toString())
    br.close();
    fr.close();

//println(outData.toString());
    is = new ByteArrayInputStream(outData.toString().getBytes());
    dataContext.storeStream(is, props);
}

