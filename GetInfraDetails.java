package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GetInfraDetails {
	public static void main(String[] args) {
		Map<String, Object[]> dataUat = new TreeMap<String, Object[]>();
//		Map<String, Object[]> dataSit = new TreeMap<String, Object[]>();
//		Map<String, Object[]> dataDev = new TreeMap<String, Object[]>();
//		Map<String, Object[]> dataNft = new TreeMap<String, Object[]>();
		int seqUat = 0;
		int rowNumUat = 1;
		dataUat.put("1", new Object[] { "#", "Microservice Name", "FlowCount", "User", "Level of MicroService",
				"Instances", "Memory", "Build Packs" });
//		dataSit.put("1", new Object[] { "#", "Microservice Name", "FlowCount", "User", "Level of MicroService",
//				"Instances", "Memory", "Build Packs" });
//		dataDev.put("1", new Object[] { "#", "Microservice Name", "FlowCount", "User", "Level of MicroService",
//				"Instances", "Memory", "Build Packs" });
//		dataNft.put("1", new Object[] { "#", "Microservice Name", "FlowCount", "User", "Level of MicroService",
//				"Instances", "Memory", "Build Packs" });
		File folder = new File("D:\\Microservices\\Output\\");
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String inputFileName = file.getName();
				System.out.println(inputFileName);
				// Try block to check for exceptions
				try {

					// Reading file from local directory
					FileInputStream excelFile = new FileInputStream(
							new File("D:\\Microservices\\Output\\" + inputFileName));

					// Create Workbook instance holding reference to
					// .xlsx file
					XSSFWorkbook workbook = new XSSFWorkbook(excelFile);

					// Get first/desired sheet from the workbook
					XSSFSheet sheet = workbook.getSheetAt(0);

					String instanceValue = "";
					String buildPackValue = "";
					String memoryValue = "";
					for (Row row : sheet) {
						Cell environmentValue = row.getCell(0);
						if (environmentValue.getStringCellValue().contains("uat")) {
								instanceValue = String.valueOf(row.getCell(1));
								buildPackValue = String.valueOf(row.getCell(2));
								memoryValue = String.valueOf(row.getCell(3));
						}

					}
					dataUat.put(String.valueOf(rowNumUat + 1), new Object[] { String.valueOf(seqUat + 1),
							String.valueOf(inputFileName.split("\\.")[0].replaceAll("-Infra", "")), "", "", "", instanceValue, memoryValue, buildPackValue });
                    rowNumUat++;
                    seqUat++;
					// Closing file output streams
					excelFile.close();
				}

				// Catch block to handle exceptions
				catch (Exception e) {

					// Display the exception along with line number
					// using printStackTrace() method
					e.printStackTrace();
				}
			}
		}
		createExcel(dataUat);

	}

	public static void createExcel(Map<String, Object[]> dataToWrite) {
		System.out.println(dataToWrite);
		XSSFWorkbook workbook = new XSSFWorkbook();

		// Create a blank sheet

		XSSFSheet sheet = workbook.createSheet("InfraDetails");

		// Iterate over data and write to sheet

		Set<String> keyset = dataToWrite.keySet();
		int rownum = 0;
		for (String key : keyset) {

			Row row = sheet.createRow(rownum++);
			Object[] objArr = dataToWrite.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Integer)
					cell.setCellValue((Integer) obj);
			}
		}

		// Write the workbook in file system

		try {
			FileOutputStream out = new FileOutputStream(
					new File("D:\\Microservices\\Microservices\\Output\\InfraDetails_UAT.xlsx"));
			workbook.write(out);
			out.close();
			System.out.println("excel written successfully on disk.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
