// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator
import com.boomi.execution.ExecutionUtil

// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/emptyfile.txt"
println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()

/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
ExecutionUtil.setDynamicProcessProperty("DPP_INVOICE_OUTPUT_FILENAME", "NOT_FOUND", false)
//document.dynamic.userdefined.XXXXXXX
dataContext.addDynamicDocumentPropertyValues(0, "DDP_Google_Filename", "lanfinffp4_20221024")


// Place script after this line.
//----------------------------------------------------------------------------------------------------

import java.util.Properties;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;



for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    String strFilepath = props.getProperty("document.dynamic.userdefined.DDP_Google_Filename");

    //DDP_Filename
    strFilepath_split = strFilepath.split("/");
    String strFilename = strFilepath_split[strFilepath_split.size()-1];

    // Informatica filename DDP_Informatica_Filename

    Date date = new Date();
    DateFormat formatter= new SimpleDateFormat("yyyyMMdd.HHmmssF");
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    String ITime;
    ITime = formatter.format(date);
    ITime = ITime.toString()


    def splittedName = strFilename.split("_");
    if (splittedName.size() > 1) {
        strLanfin = splittedName[0];
    } else {
        strLanfin = strFilename
    }
    switch (strLanfin){
        case "lanfinffp1":
            Informatica_Filename = "AYPPK.YF0102.LA_"+ITime+".success"
            break
        case "lanfinffp3":
            Informatica_Filename = "AYPPK.YF0103.LA_"+ITime+".success"
            break
        case "lanfinffp4":
            Informatica_Filename = "AYPPK.YF0106.LA_"+ITime+".success"
            break
        default:
            Informatica_Filename = ""
            break
    }
    println(Informatica_Filename)


    props.setProperty("document.dynamic.userdefined.DDP_Filename",strFilename);
    props.setProperty("document.dynamic.userdefined.DDP_Informatica_Filename",Informatica_Filename);

    dataContext.storeStream(is, props);
}