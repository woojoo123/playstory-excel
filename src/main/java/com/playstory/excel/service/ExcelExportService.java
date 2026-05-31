package com.playstory.excel.service;

import com.playstory.excel.entity.Order;
import com.playstory.excel.repository.OrderRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ExcelExportService {

    private static final int QUERY_SIZE = 1000;
    private static final int ROW_WINDOW_SIZE = 100;

    private final OrderRepository orderRepository;
    private final Path storagePath;

    public ExcelExportService(
            OrderRepository orderRepository,
            @Value("${app.excel.storage-path}") String storagePath
    ) {
        this.orderRepository = orderRepository;
        this.storagePath = Path.of(storagePath);
    }

    public String exportOrders(Long jobId) throws IOException {
        Files.createDirectories(storagePath);

        Path finalFilePath = storagePath.resolve("orders_" + jobId + ".xlsx");
        Path tempFilePath = storagePath.resolve("orders_" + jobId + ".xlsx.tmp");

        SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_WINDOW_SIZE);

        try {
            Sheet sheet = workbook.createSheet("orders");
            int rowIndex = 0;

            rowIndex = writeHeader(sheet, rowIndex);

            long lastId = 0L;

            while (true) {
                List<Order> orders =
                        orderRepository.findByIdGreaterThanOrderByIdAsc(
                                lastId,
                                PageRequest.of(0, QUERY_SIZE)
                        );

                if (orders.isEmpty()) {
                    break;
                }

                for (Order order : orders) {
                    rowIndex = writeOrder(sheet, rowIndex, order);
                }

                lastId = orders.get(orders.size() - 1).getId();
            }

            try (OutputStream outputStream =
                         Files.newOutputStream(tempFilePath)) {
                workbook.write(outputStream);
            }

            Files.move(
                    tempFilePath,
                    finalFilePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return finalFilePath.toString();
        } finally {
            try {
                workbook.close();
            } finally {
                Files.deleteIfExists(tempFilePath);
            }
        }
    }

    private int writeHeader(Sheet sheet, int rowIndex) {
        Row row = sheet.createRow(rowIndex);

        row.createCell(0).setCellValue("id");
        row.createCell(1).setCellValue("user_name");
        row.createCell(2).setCellValue("product_name");
        row.createCell(3).setCellValue("category");
        row.createCell(4).setCellValue("amount");
        row.createCell(5).setCellValue("status");
        row.createCell(6).setCellValue("order_date");

        return rowIndex + 1;
    }

    private int writeOrder(Sheet sheet, int rowIndex, Order order) {
        Row row = sheet.createRow(rowIndex);

        row.createCell(0).setCellValue(order.getId());
        row.createCell(1).setCellValue(order.getUserName());
        row.createCell(2).setCellValue(order.getProductName());
        row.createCell(3).setCellValue(order.getCategory());
        row.createCell(4).setCellValue(order.getAmount());
        row.createCell(5).setCellValue(order.getStatus());
        row.createCell(6).setCellValue(order.getOrderDate().toString());

        return rowIndex + 1;
    }
}