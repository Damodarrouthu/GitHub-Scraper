import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
 class ExcelWriter {

    private static String FILE_NAME;

    public static void setFileName(
            String fileName
    ) {

        FILE_NAME = fileName;
    }

    public static void writeData(
            String username,
            int followers,
            int repos,
            int score,
            String projects,
            String languages
    ) {

        try {

            File file =
                    new File(FILE_NAME);

            Workbook workbook;

            Sheet sheet;

            if (file.exists()) {

                FileInputStream fis =
                        new FileInputStream(file);

                workbook =
                        new XSSFWorkbook(fis);

                sheet =
                        workbook.getSheetAt(0);

                fis.close();
            }

            else {

                workbook =
                        new XSSFWorkbook();

                sheet =
                        workbook.createSheet(
                                "GitHub Analysis"
                        );

                Row header =
                        sheet.createRow(0);

                String[] titles = {

                        "Username",
                        "Followers",
                        "Repos",
                        "Score",
                        "Projects",
                        "Languages"
                };

                for (int i = 0; i < titles.length; i++) {

                    header.createCell(i)
                            .setCellValue(titles[i]);
                }
            }

            for (int i = 1;
                 i <= sheet.getLastRowNum();
                 i++) {

                Row row =
                        sheet.getRow(i);

                if (row != null &&
                        row.getCell(0)
                                .getStringCellValue()
                                .equalsIgnoreCase(username)) {

                    workbook.close();

                    return;
                }
            }

            int rowNum =
                    sheet.getLastRowNum() + 1;

            Row row =
                    sheet.createRow(rowNum);

            row.createCell(0)
                    .setCellValue(username);

            row.createCell(1)
                    .setCellValue(followers);

            row.createCell(2)
                    .setCellValue(repos);

            row.createCell(3)
                    .setCellValue(score);

            row.createCell(4)
                    .setCellValue(projects);

            row.createCell(5)
                    .setCellValue(languages);

            CellStyle style =
                    workbook.createCellStyle();

            if (score > 70) {

                style.setFillForegroundColor(
                        IndexedColors.LIGHT_GREEN.getIndex()
                );
            }

            else if (score >= 45) {

                style.setFillForegroundColor(
                        IndexedColors.LIGHT_YELLOW.getIndex()
                );
            }

            else {

                style.setFillForegroundColor(
                        IndexedColors.ROSE.getIndex()
                );
            }

            style.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            for (int i = 0; i < 6; i++) {

                row.getCell(i)
                        .setCellStyle(style);

                sheet.autoSizeColumn(i);
            }

            FileOutputStream fos =
                    new FileOutputStream(FILE_NAME);

            workbook.write(fos);

            fos.close();

            workbook.close();

            System.out.println(
                    "Saved Successfully"
            );
        }

        catch (Exception e) {

            e.printStackTrace();
        }
    }
}