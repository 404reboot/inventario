package Model;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER_SHORT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =========================================
    // REPORTE DE INVENTARIO GENERAL
    // =========================================
    public static boolean generateInventoryReport(List<Producto> products, String filePath) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Título
            addTitle(document, "REPORTE DE INVENTARIO GENERAL");
            addSubtitle(document, "Fecha de generación: " + LocalDateTime.now().format(DATE_FORMATTER));

            // Resumen
            addInventorySummary(document, products);

            // Tabla de productos
            addProductTable(document, products);
            // Pie de página
            addFooter(document);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error al generar reporte de inventario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // =========================================
    public static boolean generateLowStockReport(List<Producto> products, String filePath) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            addTitle(document, "REPORTE DE PRODUCTOS CON STOCK BAJO");
            addSubtitle(document, "Fecha de generación: " + LocalDateTime.now().format(DATE_FORMATTER));

            addLowStockSummary(document, products);
            addLowStockTable(document, products);

            addFooter(document);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error al generar reporte de stock bajo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // =========================================
    // REPORTE DE MOVIMIENTOS DE INVENTARIO
    // =========================================
    public static boolean generateMovementsReport(List<InventoryMovement> movements,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate,
                                                  String filePath) {
        try {
            Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            addTitle(document, "REPORTE DE MOVIMIENTOS DE INVENTARIO");
            String period = "Período: " + startDate.format(DATE_FORMATTER) +
                    " - " + endDate.format(DATE_FORMATTER);
            addSubtitle(document, period);

            addMovementSummary(document, movements);
            addMovementTable(document, movements);

            addFooter(document);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error al generar reporte de movimientos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // =========================================
    // REPORTE DE CATEGORÍAS
    // =========================================
    public static boolean generateCategoriesReport(List<Categoria> categories,
                                                   List<Producto> products,
                                                   String filePath) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            addTitle(document, "REPORTE DE CATEGORÍAS");
            addSubtitle(document, "Fecha de generación: " + LocalDateTime.now().format(DATE_FORMATTER));

            addCategorySummary(document, categories, products);
            addCategoryTable(document, categories, products);

            addFooter(document);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error al generar reporte de categorías: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // =========================================
    // REPORTE DE VALOR DE INVENTARIO POR CATEGORÍA
    // =========================================
    public static boolean generateInventoryValueByCategoryReport(List<Producto> products, String filePath) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            addTitle(document, "VALOR DE INVENTARIO POR CATEGORÍA");
            addSubtitle(document, "Fecha de generación: " + LocalDateTime.now().format(DATE_FORMATTER));

            // Agrupar productos por categoría
            Map<String, List<Producto>> productsByCategory = products.stream()
                    .filter(p -> p.getCategoria() != null && !p.getCategoria().isEmpty())
                    .collect(Collectors.groupingBy(Producto::getCategoria));

            addInventoryValueSummary(document, productsByCategory);
            addInventoryValueTable(document, productsByCategory);

            addFooter(document);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error al generar reporte de valor por categoría: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // =========================================
    // REPORTE DE PRODUCTOS MÁS VENDIDOS
    // =========================================
    public static boolean generateTopSellingProductsReport(List<InventoryMovement> movements, String filePath) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            addTitle(document, "REPORTE DE PRODUCTOS MÁS VENDIDOS");
            addSubtitle(document, "Fecha de generación: " + LocalDateTime.now().format(DATE_FORMATTER));

            // Filtrar solo salidas y agrupar por producto
            Map<String, Integer> productSales = movements.stream()
                    .filter(m -> m.getTipoMovimiento().equals("SALIDA"))
                    .collect(Collectors.groupingBy(
                            m -> m.getProductoNombre() != null ? m.getProductoNombre() : "Producto sin nombre",
                            Collectors.summingInt(InventoryMovement::getCantidad)
                    ));

            addTopSellingSummary(document, productSales);
            addTopSellingTable(document, productSales);

            addFooter(document);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error al generar reporte de productos más vendidos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // =========================================
    // MÉTODOS AUXILIARES PARA CONSTRUIR EL PDF
    // =========================================

    private static void addTitle(Document document, String title) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
        Paragraph titleParagraph = new Paragraph(title, titleFont);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(20);
        document.add(titleParagraph);

        // Línea separadora
        Paragraph line = new Paragraph("______________________________________________________________________________");
        line.setAlignment(Element.ALIGN_CENTER);
        line.setSpacingAfter(20);
        document.add(line);
    }

    private static void addSubtitle(Document document, String subtitle) throws DocumentException {
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
        Paragraph subtitleParagraph = new Paragraph(subtitle, subtitleFont);
        subtitleParagraph.setAlignment(Element.ALIGN_CENTER);
        subtitleParagraph.setSpacingAfter(20);
        document.add(subtitleParagraph);
    }

    private static void addFooter(Document document) throws DocumentException {
        Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);
        Paragraph footer = new Paragraph(
                "Sistema de Inventario v1.0 | Generado automáticamente | Página " +
                        document.getPageNumber(),
                footerFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }
    // =========================================
    // RESUMEN DE INVENTARIO GENERAL
    // =========================================
    private static void addInventorySummary(Document document, List<Producto> products)
            throws DocumentException {
        Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font redFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.RED);
        Font greenFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.GREEN);

        // Calcular resumen
        int totalProducts = products.size();
        int totalStock = products.stream().mapToInt(Producto::getStock).sum();
        int lowStockCount = (int) products.stream().filter(p -> p.getStock() <= p.getStock_minimo()).count();
        BigDecimal totalValue = products.stream()
                .map(p -> p.getPrecio_venta().multiply(new BigDecimal(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Crear tabla de resumen
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        float[] columnWidths = {150, 150};
        table.setWidths(columnWidths);

        // Estilo de celdas
        PdfPCell labelCell = new PdfPCell();
        labelCell.setBackgroundColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        PdfPCell valueCell = new PdfPCell();
        valueCell.setPadding(8);

        // Agregar filas
        addSummaryRow(table, "Total de productos:", String.valueOf(totalProducts));
        addSummaryRow(table, "Total de unidades en stock:", String.valueOf(totalStock));

        // Stock bajo con color
        labelCell = new PdfPCell(new Phrase("Productos con stock bajo:", summaryFont));
        labelCell.setBackgroundColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell lowStockCell = new PdfPCell();
        lowStockCell.setPadding(8);
        if (lowStockCount > 0) {
            lowStockCell.addElement(new Phrase(String.valueOf(lowStockCount), redFont));
        } else {
            lowStockCell.addElement(new Phrase(String.valueOf(lowStockCount), greenFont));
        }
        table.addCell(lowStockCell);

        addSummaryRow(table, "Valor total del inventario:", "$" + totalValue.toString());

        document.add(table);
    }
    private static void addSummaryRow(PdfPTable table, String label, String value)
            throws DocumentException {
        Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, summaryFont));
        labelCell.setBackgroundColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }
    // =========================================
    // RESUMEN DE STOCK BAJO
    // =========================================
    private static void addLowStockSummary(Document document, List<Producto> products)
            throws DocumentException {
        Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font warningFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.RED);
        Font greenFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.GREEN);

        int lowStockCount = products.size();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        float[] columnWidths = {150, 150};
        table.setWidths(columnWidths);

        PdfPCell labelCell = new PdfPCell(new Phrase("Productos con stock bajo:", summaryFont));
        labelCell.setBackgroundColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell();
        valueCell.setPadding(8);
        if (lowStockCount > 0) {
            valueCell.addElement(new Phrase(String.valueOf(lowStockCount) + " !", warningFont));
        } else {
            valueCell.addElement(new Phrase("0 ✔", greenFont));
        }
        table.addCell(valueCell);

        document.add(table);
    }
    // RESUMEN DE MOVIMIENTOS
    // =========================================
    private static void addMovementSummary(Document document, List<InventoryMovement> movements)
            throws DocumentException {
        Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font greenFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.GREEN);
        Font redFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.RED);

        int totalEntradas = (int) movements.stream()
                .filter(m -> m.getTipoMovimiento().equals("ENTRADA"))
                .count();
        int totalSalidas = (int) movements.stream()
                .filter(m -> m.getTipoMovimiento().equals("SALIDA"))
                .count();
        int totalCantidadEntradas = movements.stream()
                .filter(m -> m.getTipoMovimiento().equals("ENTRADA"))
                .mapToInt(InventoryMovement::getCantidad)
                .sum();
        int totalCantidadSalidas = movements.stream()
                .filter(m -> m.getTipoMovimiento().equals("SALIDA"))
                .mapToInt(InventoryMovement::getCantidad)
                .sum();
        BigDecimal valorEntradas = movements.stream()
                .filter(m -> m.getTipoMovimiento().equals("ENTRADA"))
                .map(InventoryMovement::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorSalidas = movements.stream()
                .filter(m -> m.getTipoMovimiento().equals("SALIDA"))
                .map(InventoryMovement::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        float[] columnWidths = {150, 150};
        table.setWidths(columnWidths);

        addSummaryRow(table, "Total de movimientos:", String.valueOf(movements.size()));
        addSummaryRow(table, "Total de entradas:", String.valueOf(totalEntradas) + " (unidades: " + totalCantidadEntradas + ")");
        addSummaryRow(table, "Total de salidas:", String.valueOf(totalSalidas) + " (unidades: " + totalCantidadSalidas + ")");

        // Valor de entradas
        PdfPCell labelCell = new PdfPCell(new Phrase("Valor de entradas:", summaryFont));
        labelCell.setBackgroundColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell();
        valueCell.setPadding(8);
        valueCell.addElement(new Phrase("$" + valorEntradas.toString(), greenFont));
        table.addCell(valueCell);

        // Valor de salidas
        labelCell = new PdfPCell(new Phrase("Valor de salidas:", summaryFont));
        labelCell.setBackgroundColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        valueCell = new PdfPCell();
        valueCell.setPadding(8);
        valueCell.addElement(new Phrase("$" + valorSalidas.toString(), redFont));
        table.addCell(valueCell);

        document.add(table);
    }
    // =========================================
    // RESUMEN DE CATEGORÍAS
    // =========================================
    private static void addCategorySummary(Document document, List<Categoria> categories,
                                           List<Producto> products) throws DocumentException {
        Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

        int totalCategories = categories.size();
        int totalProducts = products.size();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        float[] columnWidths = {150, 150};
        table.setWidths(columnWidths);

        addSummaryRow(table, "Total de categorías:", String.valueOf(totalCategories));
        addSummaryRow(table, "Total de productos:", String.valueOf(totalProducts));

        document.add(table);
    }
    // =========================================
    // TABLA DE PRODUCTOS
    // =========================================
    private static void addProductTable(Document document, List<Producto> products)
            throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        float[] columnWidths = {30, 70, 120, 80, 70, 50, 60};
        table.setWidths(columnWidths);

        // Encabezados
        String[] headers = {"ID", "Código", "Nombre", "Categoría", "Precio Venta", "Stock", "Estado"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header,
                    new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
        // Datos
        for (Producto product : products) {
            table.addCell(String.valueOf(product.getId()));
            table.addCell(product.getCodigo());
            table.addCell(product.getNombre());
            table.addCell(product.getCategoria() != null ? product.getCategoria() : "Sin categoría");
            table.addCell("$" + product.getPrecio_venta().toString());
            table.addCell(String.valueOf(product.getStock()));

            // Estado
            String estado = product.getStock() <= product.getStock_minimo() ? "Bajo" : "Normal";
            PdfPCell estadoCell = new PdfPCell(new Phrase(estado));
            if (product.getStock() <= product.getStock_minimo()) {
                estadoCell.setBackgroundColor(Color.RED);
                estadoCell.getPhrase().setFont(new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE));
            } else {
                estadoCell.setBackgroundColor(Color.GREEN);
                estadoCell.getPhrase().setFont(new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE));
            }
            estadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            estadoCell.setPadding(5);
            table.addCell(estadoCell);
        }

        document.add(table);
    }
    // =========================================
    // TABLA DE STOCK BAJO
    // =========================================
    private static void addLowStockTable(Document document, List<Producto> products)
            throws DocumentException {
        if (products.isEmpty()) {
            Paragraph noData = new Paragraph("No hay productos con stock bajo.");
            noData.setAlignment(Element.ALIGN_CENTER);
            noData.setSpacingAfter(10);
            document.add(noData);
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        float[] columnWidths = {30, 70, 120, 50, 50, 80};
        table.setWidths(columnWidths);

        // Encabezados
        String[] headers = {"ID", "Código", "Nombre", "Stock", "Mínimo", "Diferencia"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header,
                    new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(Color.RED);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Datos
        for (Producto product : products) {
            if (product.getStock() <= product.getStock_minimo()) {
                table.addCell(String.valueOf(product.getId()));
                table.addCell(product.getCodigo());
                table.addCell(product.getNombre());
                table.addCell(String.valueOf(product.getStock()));
                table.addCell(String.valueOf(product.getStock_minimo()));

                int diferencia = product.getStock_minimo() - product.getStock();
                PdfPCell diffCell = new PdfPCell(new Phrase("Faltan " + diferencia + " unidades"));
                diffCell.setBackgroundColor(Color.RED);
                diffCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                diffCell.getPhrase().setFont(new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE));
                table.addCell(diffCell);
            }
        }

        document.add(table);
    }

    // =========================================
    // TABLA DE MOVIMIENTOS
    // =========================================
    private static void addMovementTable(Document document, List<InventoryMovement> movements)
            throws DocumentException {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        float[] columnWidths = {25, 60, 120, 40, 60, 70, 100, 80, 100};
        table.setWidths(columnWidths);

        // Encabezados
        String[] headers = {"ID", "Tipo", "Producto", "Cant.", "Precio", "Subtotal", "Motivo", "Usuario", "Fecha"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header,
                    new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Datos
        for (InventoryMovement movement : movements) {
            table.addCell(String.valueOf(movement.getId()));

            // Tipo con color
            PdfPCell tipoCell = new PdfPCell(new Phrase(movement.getTipoMovimiento()));
            if (movement.getTipoMovimiento().equals("ENTRADA")) {
                tipoCell.setBackgroundColor(new Color(144, 238, 144)); // Verde claro
            } else {
                tipoCell.setBackgroundColor(new Color(255, 182, 193)); // Rojo claro
            }
            tipoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tipoCell.setPadding(5);
            table.addCell(tipoCell);

            table.addCell(movement.getProductoNombre() != null ? movement.getProductoNombre() : "N/A");
            table.addCell(String.valueOf(movement.getCantidad()));
            table.addCell("$" + movement.getPrecioUnitario().toString());
            table.addCell("$" + movement.getSubtotal().toString());
            table.addCell(movement.getMotivo() != null ? movement.getMotivo() : "-");
            table.addCell(movement.getUsuarioNombre() != null ? movement.getUsuarioNombre() : "-");
            table.addCell(movement.getFechaMovimiento() != null ?
                    movement.getFechaMovimiento().format(DATE_FORMATTER) : "-");
        }

        document.add(table);
    }

    // =========================================
    // TABLA DE CATEGORÍAS
    // =========================================
    private static void addCategoryTable(Document document, List<Categoria> categories,
                                         List<Producto> products) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        float[] columnWidths = {30, 150, 250, 80};
        table.setWidths(columnWidths);

        // Encabezados
        String[] headers = {"ID", "Nombre", "Descripción", "Productos"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header,
                    new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Datos
        for (Categoria category : categories) {
            table.addCell(String.valueOf(category.getId()));
            table.addCell(category.getNombre());
            table.addCell(category.getDescription() != null ? category.getDescription() : "-");

            // Contar productos de esta categoría
            long count = products.stream()
                    .filter(p -> p.getCategoria_id() == category.getId())
                    .count();
            table.addCell(String.valueOf(count));
        }

        document.add(table);
    }
    // =========================================
    // TABLA DE VALOR DE INVENTARIO POR CATEGORÍA
    // =========================================
    private static void addInventoryValueSummary(Document document,
                                                 Map<String, List<Producto>> productsByCategory)
            throws DocumentException {
        Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD);

        int totalCategories = productsByCategory.size();
        int totalProducts = productsByCategory.values().stream()
                .mapToInt(List::size)
                .sum();

        BigDecimal totalValue = productsByCategory.values().stream()
                .flatMap(List::stream)
                .map(p -> p.getPrecio_venta().multiply(new BigDecimal(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        float[] columnWidths = {150, 150};
        table.setWidths(columnWidths);

        addSummaryRow(table, "Total de categorías con productos:", String.valueOf(totalCategories));
        addSummaryRow(table, "Total de productos:", String.valueOf(totalProducts));
        addSummaryRow(table, "Valor total del inventario:", "$" + totalValue.toString());

        document.add(table);
    }

    private static void addInventoryValueTable(Document document,
                                               Map<String, List<Producto>> productsByCategory)
            throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        float[] columnWidths = {150, 80, 80, 100};
        table.setWidths(columnWidths);

        // Encabezados
        String[] headers = {"Categoría", "Productos", "Unidades", "Valor Total"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header,
                    new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Datos
        for (Map.Entry<String, List<Producto>> entry : productsByCategory.entrySet()) {
            String categoryName = entry.getKey();
            List<Producto> categoryProducts = entry.getValue();

            int productCount = categoryProducts.size();
            int totalStock = categoryProducts.stream().mapToInt(Producto::getStock).sum();
            BigDecimal categoryValue = categoryProducts.stream()
                    .map(p -> p.getPrecio_venta().multiply(new BigDecimal(p.getStock())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            table.addCell(categoryName);
            table.addCell(String.valueOf(productCount));
            table.addCell(String.valueOf(totalStock));
            table.addCell("$" + categoryValue.toString());
        }

        document.add(table);
    }

    // =========================================
    // TABLA DE PRODUCTOS MÁS VENDIDOS
    // =========================================
    private static void addTopSellingSummary(Document document, Map<String, Integer> productSales)
            throws DocumentException {
        Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD);

        int totalProducts = productSales.size();
        int totalUnitsSold = productSales.values().stream().mapToInt(Integer::intValue).sum();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        float[] columnWidths = {150, 150};
        table.setWidths(columnWidths);

        addSummaryRow(table, "Total de productos vendidos:", String.valueOf(totalProducts));
        addSummaryRow(table, "Total de unidades vendidas:", String.valueOf(totalUnitsSold));

        document.add(table);
    }

    private static void addTopSellingTable(Document document, Map<String, Integer> productSales)
            throws DocumentException {
        if (productSales.isEmpty()) {
            Paragraph noData = new Paragraph("No hay ventas registradas.");
            noData.setAlignment(Element.ALIGN_CENTER);
            noData.setSpacingAfter(10);
            document.add(noData);
            return;
        }

        // Ordenar por cantidad vendida (mayor a menor)
        List<Map.Entry<String, Integer>> sortedSales = productSales.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(20) // Top 20
                .toList();

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        float[] columnWidths = {30, 250, 80};
        table.setWidths(columnWidths);

        // Encabezados
        String[] headers = {"#", "Producto", "Unidades Vendidas"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header,
                    new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Datos
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedSales) {
            table.addCell(String.valueOf(rank));
            table.addCell(entry.getKey());

            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(entry.getValue())));
            if (rank <= 3) {
                cell.setBackgroundColor(new Color(255, 215, 0)); // Oro para top 3
                cell.getPhrase().setFont(new Font(Font.HELVETICA, 10, Font.BOLD));
            }
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);

            rank++;
        }

        document.add(table);
    }
}
