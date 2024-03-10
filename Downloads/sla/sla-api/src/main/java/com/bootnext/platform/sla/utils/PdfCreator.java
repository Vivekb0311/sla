// package com.bootnext.platform.sla.utils;

// import static net.logstash.logback.argument.StructuredArguments.kv;

// import java.awt.Color;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.net.MalformedURLException;
// import java.util.List;

// import com.lowagie.text.Document;
// import com.lowagie.text.DocumentException;
// import com.lowagie.text.Element;
// import com.lowagie.text.Font;
// import com.lowagie.text.Image;
// import com.lowagie.text.PageSize;
// import com.lowagie.text.Phrase;
// import com.lowagie.text.pdf.BaseFont;
// import com.lowagie.text.pdf.GrayColor;
// import com.lowagie.text.pdf.PdfContentByte;
// import com.lowagie.text.pdf.PdfPCell;
// import com.lowagie.text.pdf.PdfPTable;
// import com.lowagie.text.pdf.PdfWriter;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;

// /**
//  * The Class PdfCreator.
//  */
// public class PdfCreator {

//     /** The logger. */
//     private static Logger logger = LogManager.getLogger(PdfCreator.class);
//     private static String FONTS = SlaUtils.getConfigProp(SlaUtils.BASE_WAR_DIRECTORY) + SlaUtils.FONTS;

//     /**
//      * Creates the header table.
//      *
//      * @return the pdf P table
//      * @throws DocumentException the document exception
//      */
//     public static PdfPTable createHeaderTable() throws DocumentException {
//         PdfPTable table = new PdfPTable(2);

//         table.setWidthPercentage(100);

//         table.setWidths(new float[] { 1.8f, 6.0f });

//         Font font = new Font(Font.HELVETICA, 14, Font.BOLD, GrayColor.WHITE);
//         PdfPCell cell = new PdfPCell(new Phrase(" Project Data", font));
//         cell.setColspan(2);
//         headerCellStyle(cell);
//         table.addCell(cell);
//         return table;
//     }

//     /**
//      * Header cell style.
//      *
//      * @param cell the cell
//      */
//     public static void headerCellStyle(PdfPCell cell) {

//         cell.setHorizontalAlignment(Element.ALIGN_CENTER);

//         cell.setPaddingTop(0f);
//         cell.setPaddingBottom(7f);

//         cell.setBackgroundColor(new GrayColor(250));

//         cell.setBorder(0);
//         cell.setBorderWidthBottom(2f);

//     }

//     /**
//      * Label cell style.
//      *
//      * @param cell    the cell
//      * @param isFirst the is first
//      */
//     public static void labelCellStyle(PdfPCell cell, boolean isFirst) {
//         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
//         cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

//         cell.setPaddingLeft(3f);
//         cell.setPaddingTop(0f);

//         // cell.setBackgroundColor(new GrayColor(242));
//         cell.setBackgroundColor(new Color(242, 242, 242, 242));

//         cell.setBorder(0);
//         cell.setBorderWidthBottom(1);
//         if (isFirst) {
//             cell.setBorderWidthTop(1);
//         }
//         cell.setBorderWidthLeft(1);
//         cell.setBorderColor(GrayColor.GRAY);

//         // height
//         cell.setMinimumHeight(18f);
//     }

//     /**
//      * Value cell style.
//      *
//      * @param cell    the cell
//      * @param isFirst the is first
//      */
//     public static void valueCellStyle(PdfPCell cell, boolean isFirst) {

//         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
//         cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

//         cell.setPaddingTop(0f);
//         cell.setPaddingBottom(5f);

//         cell.setBorder(0);
//         cell.setBorderWidthBottom(1);
//         if (isFirst) {
//             cell.setBorderWidthTop(1);
//         }
//         cell.setBorderWidthRight(1);
//         cell.setBorderColor(GrayColor.GRAY);

//         cell.setMinimumHeight(18f);
//     }

//     /**
//      * Creates the empty cell.
//      *
//      * @param text the text
//      * @return the pdf P cell
//      */
//     /* create cells for empty */
//     private static PdfPCell createEmptyCell(String text) {

//         Font font = new Font(Font.HELVETICA, 8, Font.NORMAL, GrayColor.BLACK);

//         PdfPCell cell = new PdfPCell(new Phrase(text, font));

//         emptyCellStyle(cell);
//         return cell;
//     }

//     /**
//      * Empty cell style.
//      *
//      * @param cell the cell
//      */
//     public static void emptyCellStyle(PdfPCell cell) {

//         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
//         cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

//         cell.setPaddingTop(0f);
//         cell.setPaddingBottom(5f);

//         cell.setBorder(0);

//         cell.setMinimumHeight(18f);
//     }

//     /**
//      * Creates the label cell.
//      *
//      * @param text    the text
//      * @param isFirst the is first
//      * @return the pdf P cell
//      */
//     /* create cells for lable */
//     private static PdfPCell createLabelCell(String text, boolean isFirst) throws IOException {
//         BaseFont bf = null;
//         PdfPCell cell = null;
//         try {
//             bf = BaseFont.createFont(FONTS, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//             Font font = new Font(bf, 8, Font.BOLD, Color.DARK_GRAY);
//             cell = new PdfPCell(new Phrase(text, font));
//             labelCellStyle(cell, isFirst);
//             return cell;
//         } catch (DocumentException e) {

//             return cell;
//         }
//     }

//     /**
//      * Creates the value cell.
//      *
//      * @param text    the text
//      * @param isFirst the is first
//      * @return the pdf P cell
//      */
//     /* create cells for value */
//     private static PdfPCell createValueCell(String text, boolean isFirst) throws IOException {
//         BaseFont bf = null;
//         PdfPCell cell = null;
//         try {
//             bf = BaseFont.createFont(FONTS, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//             Font font = new Font(bf, 8, Font.NORMAL, GrayColor.BLACK);
//             cell = new PdfPCell(new Phrase(text, font));
//             valueCellStyle(cell, isFirst);
//             return cell;
//         } catch (DocumentException e) {

//             return cell;
//         }
//     }

//     /**
//      * Export data as pdf.
//      *
//      * @param headers               the headers
//      * @param stringArrayList       the string array list
//      * @param destinationFolderPath the destination folder path
//      * @param outPutFileName        the out put file name
//      * @param reportTitle           the report title
//      * @return the string
//      * @throws DocumentException     the document exception
//      * @throws MalformedURLException the malformed URL exception
//      * @throws IOException           Signals that an I/O exception has occurred.
//      */
//     public static String exportDataAsPdf(String headers[], List<String[]> stringArrayList, String destinationFolderPath,
//             String outPutFileName, String reportTitle) throws DocumentException, MalformedURLException, IOException {

//         String name = destinationFolderPath + outPutFileName;
//         String mainPageImagePath = SlaUtils.getConfigProp(SlaUtils.BASE_WAR_DIRECTORY)
//                 + "/sfapp/assets/img/sfProduct-report.png";
//         com.lowagie.text.Document document = new com.lowagie.text.Document();
//         document.setPageSize(PageSize.A4);

//         PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(name));
//         PdfHeaderFooterPageEvent event = new PdfHeaderFooterPageEvent();
//         pdfWriter.setPageEvent(event);
//         document.open();
//         PdfContentByte canvas = pdfWriter.getDirectContentUnder();
//         Image image = Image.getInstance(mainPageImagePath);
//         // image.scaleAbsolute(PageSize.A4);
//         image.scaleAbsoluteHeight(PageSize.A4.getHeight());
//         image.scaleAbsoluteWidth(PageSize.A4.getWidth());
//         image.setAbsolutePosition(0, 0);
//         canvas.addImage(image);
//         setReportTitle(document, pdfWriter, reportTitle);
//         document.newPage();

//         try {

//             for (String[] strings : stringArrayList) {
//                 document.add(createTable(strings, headers));
//             }
//         } catch (Exception e) {
//             logger.error(SlaUtils.ERROR_OCCURRED, "create",

//                     kv(SlaUtils.EXCEPTION_MSG, e.getMessage()));
//         }
//         document.close();
//         return outPutFileName;
//     }

//     /**
//      * Creates the table.
//      *
//      * @param values  the values
//      * @param headers the headers
//      * @return the pdf P table
//      * @throws DocumentException the document exception
//      */
//     private static PdfPTable createTable(String[] values, String[] headers) throws DocumentException {
//         // create 6 column table
//         PdfPTable table = new PdfPTable(2);

//         table.setSplitRows(false);

//         table.setHeaderRows(1);

//         // set the width of the table to 100% of page
//         table.setWidthPercentage(100);

//         // set relative columns width
//         table.setWidths(new float[] { 1.8f, 6.0f });
//         table.addCell(createEmptyCell(" "));
//         table.addCell(createEmptyCell(" "));
//         for (int i = 0; i < headers.length; i++) {
//             if (i == 0) {
//                 try {
//                     table.addCell(createLabelCell(headers[i], true));
//                     table.addCell(createValueCell(values[i], true));
//                 } catch (IOException e) {

//                 }
//             } else {
//                 try {
//                     table.addCell(createLabelCell(headers[i], false));
//                     table.addCell(createValueCell(values[i], false));
//                 } catch (IOException e) {

//                 }
//             }
//         }

//         return table;

//     }

//     /**
//      * Sets the report title.
//      *
//      * @param document  the document
//      * @param pdfWriter the pdf writer
//      * @param title     the title
//      * @throws DocumentException the document exception
//      */
//     public static void setReportTitle(Document document, PdfWriter pdfWriter, String title) throws DocumentException {
//         PdfPTable titleTable = new PdfPTable(1);
//         titleTable.setWidths(new float[] { 8.0f });
//         titleTable.setTotalWidth(document.getPageSize().getWidth());
//         titleTable.setLockedWidth(true);
//         Font font = new Font(Font.COURIER, 20, Font.NORMAL, new GrayColor(250));
//         PdfPCell cell = new PdfPCell(new Phrase(title, font));
//         cell.setFixedHeight(25);
//         cell.setBorder(0);
//         cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//         titleTable.addCell(cell);
//         titleTable.writeSelectedRows(0, -1, 0, 420, pdfWriter.getDirectContent());
//     }

// }
