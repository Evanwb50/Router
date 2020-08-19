package com.brillew.app;

/*
 * This class is used to interface with the excel file being used as the database. In the constructor it loads all the addresses
 * into an arraylist. This class also contains a method to return the address at a specific index within that arraylist for quick access
 * from within the RouterMain class. Finally, this class contains a method to add new entries to the excel sheet that is being used to store
 * addresses/customer info.
 */

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DatabaseInterface {


    public static final String filePath = "C:/Users/" + System.getProperty("user.name") + "/Desktop/HELP/TestOnline.xlsx";
    ArrayList<String> addressList;

    public DatabaseInterface() throws IOException {
        addressList = new ArrayList<>();
        Workbook wb = WorkbookFactory.create(new File(filePath));
        Sheet clients = wb.getSheetAt(0);
        DataFormatter df = new DataFormatter();
        for (int i = 1; i < clients.getLastRowNum() + 1; i++) {
            String address = "";
            for (int k = 1; k < 4; k++) {
                String cellValue = df.formatCellValue(clients.getRow(i).getCell(k));
                address = address + " " + cellValue;
            }
            addressList.add(address);
        }
        wb.close();
    }

    public String getAddresses(int rowNumber) throws IOException {
        return addressList.get(rowNumber);
    }

    public boolean addCustomer(String name, String address, String city, String state, int zip) throws IOException {
        Workbook wb = WorkbookFactory.create(new File(filePath));
        Sheet clients = wb.getSheetAt(0);
        int rowNum = clients.getLastRowNum() + 1;
        Row newRow = clients.createRow(rowNum);
        newRow.createCell(0).setCellValue(name);
        newRow.createCell(1).setCellValue(address);
        newRow.createCell(2).setCellValue(city);
        newRow.createCell(3).setCellValue(state);
        newRow.createCell(4).setCellValue(zip);
        FileOutputStream fileOut = new FileOutputStream("C:/Users/" + System.getProperty("user.name") + "/Desktop/HELP/TestOnlineNew.xlsx");
        wb.write(fileOut);
        fileOut.close();
        wb.close();
        Files.delete((Paths.get("C:/Users/" + System.getProperty("user.name") + "/Desktop/HELP/TestOnline.xlsx")));
        new File("C:/Users/" + System.getProperty("user.name") + "/Desktop/HELP/TestOnlineNew.xlsx").renameTo(new File(filePath));
        return true;
    }
}
