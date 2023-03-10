// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator
import com.boomi.execution.ExecutionUtil
import org.apache.tools.ant.types.selectors.SelectSelector

// Place directory for multiple files and file name for single file
// String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/heavy_test.xlsx"
//String pathFiles = "c:/users/ay54337/downloads/Account statement transaction 20221102T095852.xlsx"
String pathFiles = "C:/Users/AY54337/Downloads/Sharepoint client secret renewals - 2023-01.xlsx"
println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()

/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
ExecutionUtil.setDynamicProcessProperty("DPP_name", "DPP_value", false)
dataContext.addDynamicDocumentPropertyValues(0, "DDP_CSVDelimiter", ",")
dataContext.addDynamicDocumentPropertyValues(0, "DDP_CSVTextQualifier", '"')
dataContext.addDynamicDocumentPropertyValues(0, "DDP_CSVLastLineBreak", "false")
/*
dataContext.addDynamicDocumentPropertyValues(0, "DDP_CSVNewLine", "\r\n")
dataContext.addDynamicDocumentPropertyValues(0, "DDP_CSVLineBreaks", "false")
dataContext.addDynamicDocumentPropertyValues(0, "DDP_CSVLastLineBreak", "true")
*/

// Place script after this line.
//----------------------------------------------------------------------------------------------------
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    String strDelimiter = props.getProperty("document.dynamic.userdefined.DDP_CSVDelimiter");
    if (!strDelimiter)
        strDelimiter = ";"

    String strTextQualifier = props.getProperty("document.dynamic.userdefined.DDP_CSVTextQualifier");
    if (!strTextQualifier)
        strTextQualifier = ""

    String strNewLine = props.getProperty("document.dynamic.userdefined.DDP_CSVNewLine")
    if (!strNewLine)
        strNewLine = "\r\n"

    String strLastLineBreak = props.getProperty("document.dynamic.userdefined.DDP_CSVLastLineBreak")
    if (!strLastLineBreak)
        strLastLineBreak = "true"
    else {
        if(strLastLineBreak != "true")
            strLastLineBreak = "false"
    }

    String strAllowLineBreaks = props.getProperty("document.dynamic.userdefined.DDP_CSVLineBreaks")
    if (!strAllowLineBreaks)
        strAllowLineBreaks = "false"
    else {
        if (strAllowLineBreaks != "true")
            strAllowLineBreaks = "false"
    }


    DataFormatter formatter = new DataFormatter(Locale.getDefault());
    Workbook wb = WorkbookFactory.create(is);
    List sheetList = wb.sheets;
    StringBuilder sb = new StringBuilder();
    int iRow = 0;
    int numOfColumns = 0;
    int iCol = 0;

    for (int j = 0; j < sheetList.size(); j++) {
        Sheet sheet = wb.getSheetAt(j);
        numOfColumns = sheet.getRow(0).getLastCellNum();
        for (Row row: sheet) {
            iCol = 0
            for (Cell cell: row) {
                iCol++
                switch (cell.getCellType()) {
                    case cell.cellType.NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            sb.append(strTextQualifier + formatter.formatCellValue(cell) + strTextQualifier);
                        } else {
                            sb.append(strTextQualifier + cell.getNumericCellValue() + strTextQualifier);
                        }
                        break;
                    case cell.cellType.STRING:
                        if (strAllowLineBreaks == "true") {
                            sb.append(strTextQualifier + cell.getStringCellValue() + strTextQualifier);
                        }
                        else {
                            sb.append(strTextQualifier + cell.getStringCellValue().replaceAll("\r"," ").replaceAll("\n"," ") + strTextQualifier);
                        }
                        break;
                    case cell.cellType.FORMULA:
                        sb.append(strTextQualifier + cell.getCellFormula() + strTextQualifier);
                        break;
                    case cell.cellType.BOOLEAN:
                        sb.append(strTextQualifier + cell.getBooleanCellValue() + strTextQualifier);
                        break;
                    case cell.cellType.BLANK:
                        sb.append("");
                        break;
                    default:
                        sb.append("");
                        break;
                }
                sb.append(strDelimiter);
            }
            for (; iCol < numOfColumns; iCol++) {
                sb.append("");
                sb.append(strDelimiter);
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - strDelimiter.length());
            }
            sb.append(strNewLine);
        }

        String output = sb.toString();
        if(strLastLineBreak == "false") {
            int lastlinebreak = output.length()-strNewLine.length()
            output = output.substring(0,lastlinebreak)
        }
        print(output)
        sb.setLength(0);
        is = new ByteArrayInputStream(output.getBytes());
        props.setProperty("document.dynamic.userdefined.SHEET", sheet.getSheetName());
        dataContext.storeStream(is, props);}
}


