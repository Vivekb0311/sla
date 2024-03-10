package com.bootnext.platform.sla.utils;



import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

public class ExcelUtils {

    private static Logger logger = LogManager.getLogger(ExcelUtils.class);

    private ExcelUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final int FONT_HEIGHT_IN_POINTS = 3;

    public static String getDataInXlsx(List<String[]> stringList, String root, String fileName) throws IOException {
        fileName = fileName + "_" + SlaUtils.getDateFormatforExport() + ".xlsx";
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet(fileName);
        createHeading(stringList.remove(0), sheet1, wb);
        createRows(stringList, sheet1, getdetailsDataStyle(wb));
            FileOutputStream out = new FileOutputStream(new File(root + fileName));
            wb.write(out);
            out.close();
        return fileName;
    }

    public static void createHeading(String[] header, Sheet sheet1, Workbook wb) {
        Row heading = sheet1.createRow(0);
        for (int i = 0; i < header.length; i++) {
            Cell headingCell = heading.createCell(i);
            headingCell.setCellValue(header[i]);
            headingCell.setCellStyle(getHeadingStyle(wb));
            sheet1.autoSizeColumn(i);
        }
    }

    public static CellStyle getdetailsDataStyle(Workbook wb) {
        Font font = wb.createFont();
        DataFormat fmt = wb.createDataFormat();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setDataFormat(fmt.getFormat("@"));
        return cellStyle;
    }

    public static CellStyle getHeadingStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;

    }

    public static void createRows(List<String[]> dataList, Sheet sheet, CellStyle cellStyle) {
        logger.info(SlaUtils.INSIDE_METHOD, "createRows");
        Row datarow = null;
        int i = 1;
        for (String[] data : dataList) {
            datarow = sheet.createRow(i);
            for (int j = 0; j < data.length; j++) {
                Cell cell = datarow.createCell(j);
                setCellValue(data[j], cell);
                if (cellStyle != null) {
                    cell.setCellStyle(cellStyle);
                }
            }
            i++;
        }
    }

    private static void setCellValue(String string, Cell cell) {
        if (NumberUtils.isDigits(string)) {
            try {
                cell.setCellValue(Long.valueOf(string));
            } catch (Exception e) {
                cell.setCellValue(string);
            }
        } else if (NumberUtils.isCreatable(string)) {
            try {
                cell.setCellValue(Double.valueOf(string));
            } catch (Exception e) {
                cell.setCellValue(string);
            }
        } else {
            cell.setCellValue(string);
        }
    }

    public static String evaluateCellFormula(Cell cell) {
        String value = "";
        switch (cell.getCachedFormulaResultType()) {
        case NUMERIC:
            value = String.valueOf(cell.getNumericCellValue());
            break;
        case STRING:
            value = cell.getStringCellValue();
            break;
        default:
            logger.info("in default");
        }
        return value;
    }

    public static void createImportSampleFromHeaders(List<String> headerList, String fileName, String filePath)
            throws IOException {
        logger.info(
                "Inside @class PMConfigUtil @method createImportSampleFromHeaders fileName {} filePath :{} headerList {}",
                fileName, filePath, headerList);
        XSSFColor black = new XSSFColor(Color.black, null);
        XSSFColor white = new XSSFColor(Color.WHITE, null);
        XSSFColor blue = new XSSFColor(Color.BLUE, null);
        XSSFColor yellow = new XSSFColor(Color.YELLOW, null);
        String importSampleFileNAme = fileName;
        if (CollectionUtils.isNotEmpty(headerList)) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            Row row = null;
            if ((row = sheet.getRow(0)) == null) {
                row = sheet.createRow(0);
            }

            XSSFCellStyle mendetoryFieldStyle = createCellStyleForWorkbook(workbook, white, blue);
            mendetoryFieldStyle.setFillBackgroundColor(blue);
            mendetoryFieldStyle.setFillPattern(FillPatternType.BIG_SPOTS);
            XSSFCellStyle optionalFieldStyle = createCellStyleForWorkbook(workbook, black, yellow);

            for (int i = 0; i < headerList.size(); i++) {
                Cell cell = row.createCell(i);
                String header = headerList.get(i);
                if (header.contains("*")) {
                    cell.setCellStyle(mendetoryFieldStyle);
                    cell.setCellValue(header.substring(0, headerList.get(i).length() - 1));
                } else {
                    cell.setCellStyle(optionalFieldStyle);
                    cell.setCellValue(header);
                }
                sheet.autoSizeColumn(i);
            }
            File fileDir = new File(filePath);
            logger.trace("Final path {}", filePath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            try (FileOutputStream outputStream = new FileOutputStream(
                    new File(fileDir + SlaUtils.PATH_DELIMITER + importSampleFileNAme))) {
                logger.trace("File PAth::::::::{} / {} ", fileDir, importSampleFileNAme);
                workbook.write(outputStream);
                workbook.close();
            }
        }
    }

    /** @Defination to set cell style */
    public static XSSFCellStyle createCellStyleForWorkbook(XSSFWorkbook workbook, XSSFColor fontColorIndex,
            XSSFColor bgColorIndex) {
        XSSFColor black = new XSSFColor(Color.black, null);
        XSSFFont fontStyle = workbook.createFont();
        fontStyle.setFontHeightInPoints((short) FONT_HEIGHT_IN_POINTS);
        fontStyle.setBold(true);
        fontStyle.setItalic(false);
        fontStyle.setColor(fontColorIndex);

        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderColor(BorderSide.BOTTOM, black);
        style.setBorderColor(BorderSide.LEFT, black);
        style.setBorderColor(BorderSide.RIGHT, black);
        style.setBorderColor(BorderSide.TOP, black);
        style.setFillForegroundColor(bgColorIndex);
        style.setFillPattern(FillPatternType.BIG_SPOTS);
        style.setFont(fontStyle);
        return style;
    }

}
