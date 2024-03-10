//package com.bootnext.platform.sla.utils;
//
//import java.awt.Color;
//import java.io.IOException;
//import java.net.MalformedURLException;
//
//import com.lowagie.text.Document;
//import com.lowagie.text.DocumentException;
//import com.lowagie.text.Element;
//import com.lowagie.text.ExceptionConverter;
//import com.lowagie.text.Font;
//import com.lowagie.text.Image;
//import com.lowagie.text.Phrase;
//import com.lowagie.text.Rectangle;
//import com.lowagie.text.pdf.PdfContentByte;
//import com.lowagie.text.pdf.PdfPCell;
//import com.lowagie.text.pdf.PdfPTable;
//import com.lowagie.text.pdf.PdfPageEventHelper;
//import com.lowagie.text.pdf.PdfTemplate;
//import com.lowagie.text.pdf.PdfWriter;
//
///**
// * The Class PdfHeaderFooterPageEvent.
// */
//public class PdfHeaderFooterPageEvent extends PdfPageEventHelper {
//
//    /** The t. */
//    private PdfTemplate t;
//
//    /** The total. */
//    private Image total;
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see
//     * com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(com.itextpdf.text.pdf
//     * .PdfWriter, com.itextpdf.text.Document)
//     */
//    @Override
//    public void onOpenDocument(PdfWriter writer, Document document) {
//        t = writer.getDirectContent().createTemplate(30, 16);
//        try {
//            total = Image.getInstance(t);
//            // total.setRole(PdfName.ar);
//        } catch (DocumentException de) {
//            throw new ExceptionConverter(de);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see
//     * com.itextpdf.text.pdf.PdfPageEventHelper#onStartPage(com.itextpdf.text.pdf.
//     * PdfWriter, com.itextpdf.text.Document)
//     */
//    @Override
//    public void onStartPage(PdfWriter writer, Document document) {
//
//        if (writer.getPageNumber() != 1) {
//            addHeader(writer, document);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see
//     * com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(com.itextpdf.text.pdf.
//     * PdfWriter, com.itextpdf.text.Document)
//     */
//    @Override
//    public void onEndPage(PdfWriter writer, Document document) {
//
//        if (writer.getPageNumber() != 1) {
//            addFooter(writer, document);
//        }
//    }
//
//    /**
//     * Adds the header.
//     *
//     * @param writer   the writer
//     * @param document the document
//     */
//    private void addHeader(PdfWriter writer, Document document) {
//        PdfPTable header = new PdfPTable(1);
//        try {
//            header.setWidths(new float[] { 8.0f });
//            header.setTotalWidth(document.getPageSize().getWidth());
//            header.setLockedWidth(true);
//            PdfPCell cell = new PdfPCell();
//            Image logo = Image.getInstance(
//                    SlaUtils.getConfigProp(SlaUtils.BASE_WAR_DIRECTORY) + "/sfapp/assets/img/platform2logo.png");
//            logo.scaleAbsolute(100, 100);
//            logo.setAbsolutePosition(10, 0);
//            cell.setBorder(0);
//            cell.setPaddingBottom(8f);
//            cell.setPaddingTop(10f);
//            cell.setPaddingLeft(460f);
//            cell.addElement(logo);
//            header.addCell(cell);
//            header.getDefaultCell().setFixedHeight(5);
//            header.getDefaultCell().setPaddingBottom(40);
//            header.getDefaultCell().setBorder(Rectangle.BOTTOM);
//            header.getDefaultCell().setBorder(1);
//            header.getDefaultCell().setBorderWidth(1);
//            header.getDefaultCell().setBorderColor(new Color(33, 150, 243, 243));
//            header.addCell("");
//            header.writeSelectedRows(0, -1, 0, 840, writer.getDirectContent());
//        } catch (DocumentException de) {
//            throw new ExceptionConverter(de);
//        } catch (MalformedURLException e) {
//            throw new ExceptionConverter(e);
//        } catch (IOException e) {
//            throw new ExceptionConverter(e);
//        }
//    }
//
//    /**
//     * Adds the footer.
//     *
//     * @param writer   the writer
//     * @param document the document
//     */
//    private void addFooter(PdfWriter writer, Document document) {
//        PdfPTable footer = new PdfPTable(1);
//        try {
//            footer.setWidths(new float[] { 6.0f });
//            footer.setTotalWidth(document.getPageSize().getWidth());
//            footer.setLockedWidth(true);
//            footer.getDefaultCell().setFixedHeight(20);
//            footer.getDefaultCell().setBorder(0);
//            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
//            footer.getDefaultCell().setPaddingRight(20);
//            footer.getDefaultCell().setBackgroundColor(new Color(33, 150, 243, 243));// new BaseColor(33,150,243));
//            footer.addCell(new Phrase(String.format("%d", writer.getPageNumber()),
//                    new Font(Font.HELVETICA, 10, Font.NORMAL, Color.WHITE)));
//            PdfPCell totalPageCount = new PdfPCell(total);
//            footer.addCell(totalPageCount);
//            PdfContentByte canvas = writer.getDirectContent();
//            // canvas.beginMarkedContentSequence(PdfName.AA);
//            // canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
//            footer.writeSelectedRows(0, -1, 0, 20, canvas);
//            // canvas.endMarkedContentSequence();
//        } catch (DocumentException de) {
//            throw new ExceptionConverter(de);
//        }
//    }
//}
