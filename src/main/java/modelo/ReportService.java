package modelo; // Ajustado a tu paquete actual
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio encargado de generar los distintos reportes del sistema de
 * inventario en formato PDF (mediante iText) y Excel/XLSX (mediante
 * Apache POI).
 * <p>
 * Cada tipo de reporte cuenta con dos métodos hermanos: uno que produce
 * un documento PDF ({@code ...Pdf}) y otro que produce un libro de
 * cálculo Excel ({@code ...Excel}), ambos consultando los mismos datos
 * en la base de datos y escribiendo el resultado en la ruta de archivo
 * indicada. Los reportes disponibles son:
 * <ol>
 *   <li>Stock actual de productos</li>
 *   <li>Movimientos de inventario en un rango de fechas</li>
 *   <li>Productos con bajo stock (por debajo de un umbral)</li>
 *   <li>Productos con mayor cantidad de movimientos (ranking)</li>
 * </ol>
 */
public class ReportService {

    /** Fuente utilizada para el título principal de los reportes PDF. */
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    /** Fuente utilizada para el subtítulo (fecha de generación, filtros aplicados, etc.) de los reportes PDF. */
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY);
    /** Fuente utilizada para los encabezados de las tablas en los reportes PDF. */
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
    /** Fuente utilizada para los datos de las celdas en los reportes PDF. */
    private static final Font DATA_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    /** Fuente en negrita utilizada para los resúmenes/totales al final de los reportes PDF. */
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);

    // ==========================================
    // 1. REPORTE DE STOCK ACTUAL (PDF Y EXCEL)
    // ==========================================

    /**
     * Genera un reporte en PDF con el stock actual de todos los productos,
     * incluyendo su categoría y precio de venta, junto con un resumen del
     * total de ítems y unidades en almacén.
     *
     * @param filePath ruta absoluta del archivo PDF de salida
     */
    public void generateCurrentStockPdf(String filePath) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Paragraph title = new Paragraph("Reporte de Stock Actual de Inventario", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Paragraph subtitle = new Paragraph("Generado el: " + currentDateTime, SUBTITLE_FONT);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.0f, 3.5f, 1.5f, 1.5f, 2.0f});

            String[] headers = {"ID", "Código", "Nombre del Producto", "Categoría", "Stock Actual", "Precio Venta"};
            for (String headerText : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(headerText, HEADER_FONT));
                headerCell.setBackgroundColor(new BaseColor(41, 128, 185));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(6);
                table.addCell(headerCell);
            }

            String query = "SELECT p.id, p.codigo, p.nombre, c.nombre AS categoria, p.stock, p.precio_venta " +
                           "FROM productos p " +
                           "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                           "ORDER BY p.nombre ASC";

            // Llama directamente a conexionDB porque está en el mismo paquete 'modelo'
            try (Connection conn = conexionDB.conectar(); 
                 PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                int totalProducts = 0;
                int totalStock = 0;

                while (rs.next()) {
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(rs.getInt("id")), DATA_FONT)));
                    table.addCell(new PdfPCell(new Phrase(rs.getString("codigo"), DATA_FONT)));
                    table.addCell(new PdfPCell(new Phrase(rs.getString("nombre"), DATA_FONT)));
                    table.addCell(new PdfPCell(new Phrase(rs.getString("categoria") != null ? rs.getString("categoria") : "Sin Categoría", DATA_FONT)));
                    
                    int stock = rs.getInt("stock");
                    PdfPCell stockCell = new PdfPCell(new Phrase(String.valueOf(stock), DATA_FONT));
                    stockCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(stockCell);

                    BigDecimal price = rs.getBigDecimal("precio_venta");
                    PdfPCell priceCell = new PdfPCell(new Phrase(price != null ? "$" + price.toString() : "$0.00", DATA_FONT));
                    priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(priceCell);

                    totalProducts++;
                    totalStock += stock;
                }

                document.add(table);

                Paragraph summary = new Paragraph("\nResumen del Reporte:\n" +
                        "Total de ítems registrados: " + totalProducts + "\n" +
                        "Cantidad total de productos en almacén: " + totalStock, BOLD_FONT);
                summary.setSpacingBefore(15);
                document.add(summary);

            } catch (SQLException e) {
                System.err.println("Error SQL en reporte de stock: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Genera un reporte en Excel (XLSX) con el stock actual de todos los
     * productos, incluyendo su categoría y precio de venta.
     *
     * @param filePath ruta absoluta del archivo XLSX de salida
     */
    public void generateCurrentStockExcel(String filePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Stock Actual");

        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Código", "Nombre", "Categoría", "Stock", "Precio Venta"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        String query = "SELECT p.id, p.codigo, p.nombre, c.nombre AS categoria, p.stock, p.precio_venta " +
                       "FROM productos p " +
                       "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                       "ORDER BY p.nombre ASC";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            int rowIdx = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rs.getInt("id"));
                row.createCell(1).setCellValue(rs.getString("codigo"));
                row.createCell(2).setCellValue(rs.getString("nombre"));
                row.createCell(3).setCellValue(rs.getString("categoria") != null ? rs.getString("categoria") : "Sin Categoría");
                row.createCell(4).setCellValue(rs.getInt("stock"));
                
                BigDecimal price = rs.getBigDecimal("precio_venta");
                row.createCell(5).setCellValue(price != null ? price.doubleValue() : 0.00);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // 2. REPORTE DE MOVIMIENTOS POR FECHAS (PDF Y EXCEL)
    // ==========================================

    /**
     * Genera un reporte en PDF (orientación horizontal) con todos los
     * movimientos de inventario ocurridos dentro del rango de fechas
     * indicado, incluyendo un resumen del total de unidades ingresadas
     * (entradas) y retiradas (salidas) en el período.
     *
     * @param filePath ruta absoluta del archivo PDF de salida
     * @param startDate fecha de inicio del rango, en formato {@code yyyy-MM-dd}
     * @param endDate fecha de fin del rango, en formato {@code yyyy-MM-dd}
     */
    public void generateMovementsReportPdf(String filePath, String startDate, String endDate) {
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Paragraph title = new Paragraph("Reporte de Movimientos de Inventario", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Rango: Desde " + startDate + " Hasta " + endDate, SUBTITLE_FONT);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 2.2f, 1.2f, 1.0f, 1.2f, 1.2f, 1.5f, 2.0f, 1.5f});

            String[] headers = {"ID", "Producto", "Tipo", "Cant.", "P. Unitario", "Subtotal", "Fecha", "Motivo", "Referencia"};
            for (String headerText : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(headerText, HEADER_FONT));
                headerCell.setBackgroundColor(new BaseColor(52, 73, 94));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(5);
                table.addCell(headerCell);
            }

            String query = "SELECT m.id, p.nombre AS producto, m.tipo_movimiento, m.cantidad, " +
                           "m.precio_unitario, (m.cantidad * m.precio_unitario) AS subtotal, m.fecha_movimiento, m.motivo, m.referencia " +
                           "FROM movimientos m " +
                           "LEFT JOIN productos p ON m.producto_id = p.id " +
                           "WHERE m.fecha_movimiento BETWEEN ? AND ? " +
                           "ORDER BY m.fecha_movimiento ASC";

            try (Connection conn = conexionDB.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, startDate + " 00:00:00");
                pstmt.setString(2, endDate + " 23:59:59");
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    int totalEntradas = 0;
                    int totalSalidas = 0;

                    while (rs.next()) {
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(rs.getInt("id")), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase(rs.getString("producto") != null ? rs.getString("producto") : "Producto Eliminado", DATA_FONT)));
                        
                        String tipo = rs.getString("tipo_movimiento");
                        table.addCell(new PdfPCell(new Phrase(tipo, DATA_FONT)));
                        
                        int cant = rs.getInt("cantidad");
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(cant), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase("$" + rs.getBigDecimal("precio_unitario").toString(), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase("$" + rs.getBigDecimal("subtotal").toString(), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase(rs.getString("fecha_movimiento"), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase(rs.getString("motivo") != null ? rs.getString("motivo") : "", DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase(rs.getString("referencia") != null ? rs.getString("referencia") : "", DATA_FONT)));

                        if ("ENTRADA".equalsIgnoreCase(tipo)) {
                            totalEntradas += cant;
                        } else if ("SALIDA".equalsIgnoreCase(tipo)) {
                            totalSalidas += cant;
                        }
                    }

                    document.add(table);

                    Paragraph summary = new Paragraph("\nResumen del Período:\n" +
                            "Total unidades ingresadas (ENTRADAS): " + totalEntradas + "\n" +
                            "Total unidades retiradas (SALIDAS): " + totalSalidas, BOLD_FONT);
                    document.add(summary);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Genera un reporte en Excel (XLSX) con todos los movimientos de
     * inventario ocurridos dentro del rango de fechas indicado.
     *
     * @param filePath ruta absoluta del archivo XLSX de salida
     * @param startDate fecha de inicio del rango, en formato {@code yyyy-MM-dd}
     * @param endDate fecha de fin del rango, en formato {@code yyyy-MM-dd}
     */
    public void generateMovementsReportExcel(String filePath, String startDate, String endDate) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Movimientos");

        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Producto", "Tipo Movimiento", "Cantidad", "Precio Unitario", "Subtotal", "Fecha", "Motivo", "Referencia"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        String query = "SELECT m.id, p.nombre AS producto, m.tipo_movimiento, m.cantidad, " +
                       "m.precio_unitario, (m.cantidad * m.precio_unitario) AS subtotal, m.fecha_movimiento, m.motivo, m.referencia " +
                       "FROM movimientos m " +
                       "LEFT JOIN productos p ON m.producto_id = p.id " +
                       "WHERE m.fecha_movimiento BETWEEN ? AND ? " +
                       "ORDER BY m.fecha_movimiento ASC";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, startDate + " 00:00:00");
            pstmt.setString(2, endDate + " 23:59:59");

            try (ResultSet rs = pstmt.executeQuery()) {
                int rowIdx = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rs.getInt("id"));
                    row.createCell(1).setCellValue(rs.getString("producto") != null ? rs.getString("producto") : "Producto Eliminado");
                    row.createCell(2).setCellValue(rs.getString("tipo_movimiento"));
                    row.createCell(3).setCellValue(rs.getInt("cantidad"));
                    row.createCell(4).setCellValue(rs.getBigDecimal("precio_unitario").doubleValue());
                    row.createCell(5).setCellValue(rs.getBigDecimal("subtotal").doubleValue());
                    row.createCell(6).setCellValue(rs.getString("fecha_movimiento"));
                    row.createCell(7).setCellValue(rs.getString("motivo") != null ? rs.getString("motivo") : "");
                    row.createCell(8).setCellValue(rs.getString("referencia") != null ? rs.getString("referencia") : "");
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // 3. REPORTE DE BAJO STOCK (PDF Y EXCEL)
    // ==========================================

    /**
     * Genera un reporte en PDF con los productos cuyo stock actual es
     * menor o igual al umbral indicado, incluyendo el total de productos
     * críticos que requieren reposición.
     *
     * @param filePath ruta absoluta del archivo PDF de salida
     * @param stockThreshold umbral máximo de stock para considerar un producto en nivel crítico
     */
    public void generateLowStockReportPdf(String filePath, int stockThreshold) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Paragraph title = new Paragraph("Alerta de Reposición - Bajo Stock", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Productos con stock menor o igual a: " + stockThreshold + " unidades", SUBTITLE_FONT);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.0f, 4.0f, 1.5f, 1.5f});

            String[] headers = {"ID", "Código", "Nombre Producto", "Stock Actual", "Categoría"};
            for (String headerText : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(headerText, HEADER_FONT));
                headerCell.setBackgroundColor(new BaseColor(192, 57, 43));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(6);
                table.addCell(headerCell);
            }

            String query = "SELECT p.id, p.codigo, p.nombre, p.stock, c.nombre AS categoria " +
                           "FROM productos p " +
                           "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                           "WHERE p.stock <= ? " +
                           "ORDER BY p.stock ASC";

            try (Connection conn = conexionDB.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setInt(1, stockThreshold);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    int lowStockCount = 0;

                    while (rs.next()) {
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(rs.getInt("id")), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase(rs.getString("codigo"), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase(rs.getString("nombre"), DATA_FONT)));
                        
                        PdfPCell stockCell = new PdfPCell(new Phrase(String.valueOf(rs.getInt("stock")), DATA_FONT));
                        stockCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        table.addCell(stockCell);
                        
                        table.addCell(new PdfPCell(new Phrase(rs.getString("categoria") != null ? rs.getString("categoria") : "Sin Categoría", DATA_FONT)));
                        
                        lowStockCount++;
                    }

                    document.add(table);

                    Paragraph summary = new Paragraph("\nTotal de productos críticos que requieren reposición inmediata: " + lowStockCount, BOLD_FONT);
                    document.add(summary);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Genera un reporte en Excel (XLSX) con los productos cuyo stock
     * actual es menor o igual al umbral indicado.
     *
     * @param filePath ruta absoluta del archivo XLSX de salida
     * @param stockThreshold umbral máximo de stock para considerar un producto en nivel crítico
     */
    public void generateLowStockReportExcel(String filePath, int stockThreshold) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Bajo Stock");

        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Código", "Nombre Producto", "Stock Actual", "Categoría"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        String query = "SELECT p.id, p.codigo, p.nombre, p.stock, c.nombre AS categoria " +
                       "FROM productos p " +
                       "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                       "WHERE p.stock <= ? " +
                       "ORDER BY p.stock ASC";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, stockThreshold);

            try (ResultSet rs = pstmt.executeQuery()) {
                int rowIdx = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rs.getInt("id"));
                    row.createCell(1).setCellValue(rs.getString("codigo"));
                    row.createCell(2).setCellValue(rs.getString("nombre"));
                    row.createCell(3).setCellValue(rs.getInt("stock"));
                    row.createCell(4).setCellValue(rs.getString("categoria") != null ? rs.getString("categoria") : "Sin Categoría");
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // 4. METRICAS / PRODUCTOS MÁS MOVIDOS (PDF Y EXCEL)
    // ==========================================

    /**
     * Genera un reporte estadístico en PDF con el ranking de los productos
     * con mayor cantidad de movimientos registrados (entradas y salidas
     * combinadas), limitado a la cantidad indicada.
     *
     * @param filePath ruta absoluta del archivo PDF de salida
     * @param limit cantidad máxima de productos a incluir en el ranking (top N)
     */
    public void generateMostMovedProductsPdf(String filePath, int limit) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Paragraph title = new Paragraph("Reporte Estadístico: Productos con Mayor Movimiento", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Top " + limit + " artículos con más actividad en almacén", SUBTITLE_FONT);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.5f, 4.0f, 2.5f});

            String[] headers = {"Puesto", "Código", "Nombre Producto", "Transacciones Totales"};
            for (String headerText : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(headerText, HEADER_FONT));
                headerCell.setBackgroundColor(new BaseColor(39, 174, 96));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(6);
                table.addCell(headerCell);
            }

            String query = "SELECT p.codigo, p.nombre, COUNT(m.id) AS total_movimientos " +
                           "FROM movimientos m " +
                           "JOIN productos p ON m.producto_id = p.id " +
                           "GROUP BY p.id, p.codigo, p.nombre " +
                           "ORDER BY total_movimientos DESC " +
                           "LIMIT ?";

            try (Connection conn = conexionDB.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setInt(1, limit);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    int rank = 1;
                    while (rs.next()) {
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(rank++), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase(rs.getString("codigo"), DATA_FONT)));
                        table.addCell(new PdfPCell(new Phrase(rs.getString("nombre"), DATA_FONT)));
                        
                        PdfPCell countCell = new PdfPCell(new Phrase(String.valueOf(rs.getInt("total_movimientos")), DATA_FONT));
                        countCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(countCell);
                    }
                    document.add(table);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Genera un reporte estadístico en Excel (XLSX) con el ranking de los
     * productos con mayor cantidad de movimientos registrados (entradas y
     * salidas combinadas), limitado a la cantidad indicada.
     *
     * @param filePath ruta absoluta del archivo XLSX de salida
     * @param limit cantidad máxima de productos a incluir en el ranking (top N)
     */
    public void generateMostMovedProductsExcel(String filePath, int limit) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos Más Movidos");

        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Puesto", "Código", "Nombre Producto", "Total Movimientos"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        String query = "SELECT p.codigo, p.nombre, COUNT(m.id) AS total_movimientos " +
                       "FROM movimientos m " +
                       "JOIN productos p ON m.producto_id = p.id " +
                       "GROUP BY p.id, p.codigo, p.nombre " +
                       "ORDER BY total_movimientos DESC " +
                       "LIMIT ?";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                int rowIdx = 1;
                int rank = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rank++);
                    row.createCell(1).setCellValue(rs.getString("codigo"));
                    row.createCell(2).setCellValue(rs.getString("nombre"));
                    row.createCell(3).setCellValue(rs.getInt("total_movimientos"));
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}