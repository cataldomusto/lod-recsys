package di.uniba.it.lodrecsys.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by asuglia on 5/5/14.
 */
public class MetricsWriter {
    private FileOutputStream excelOutputFile;
    private int rowCount;

    public MetricsWriter(String finalExcelResult) throws IOException {
        try {
            excelOutputFile = new FileOutputStream(new File(finalExcelResult));
        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            assert excelOutputFile != null;
            excelOutputFile.close();
        }

    }

    public void completeWrite() throws IOException {
        assert excelOutputFile != null;
        excelOutputFile.close();
    }

    public void write(String methodSignature, Map<String, String> metricsValues) throws IOException {

        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFSheet sheet = wb.createSheet("Evaluation");
        HSSFRow currRow;
        HSSFCell currCell;
        int colNumber = 0;
        currRow = sheet.createRow(rowCount++);
        for (String metricsName : metricsValues.keySet()) {
            currCell = currRow.createCell(colNumber);
            currCell.setCellValue(metricsName);
            colNumber++;
        }


    }
}
