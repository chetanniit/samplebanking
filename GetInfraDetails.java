package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

public class ReadInfraDetails {
	private static final List<String> keysToFind = Arrays.asList(
			 "pcf_memory", "pcf_instances", "pcf_buildpack");

	private static final String pcfConfigDirectory = "C:\\Users\\dhalv\\Downloads\\ConfigFiles\\New\\";
	private static final String resourcesDirectory = "C:\\Users\\dhalv\\Downloads\\ConfigFiles\\";

	private static final List<String> targetFilesPcf = Arrays.asList(
			"pcf-prd.config",  "pcf-uat.config",
			 "pcf-dev.config",  "pcf-sit.config",  "pcf-nft.config",
			"pcf-common.config");

	

	private static final Map<String, List<Map<String, String>>> data = new HashMap<>();
	private static final Set<String> jdbcOracleValues = new HashSet<>();

	public static void main(String[] args) throws IOException {
		File dir = new File("C:\\\\Users\\\\dhalv\\\\Downloads\\\\ConfigFiles\\\\New\\");
		File[] subdirs = dir.listFiles(File::isDirectory);

		if (subdirs != null) {
			for (File subdir : subdirs) {
				System.out.println(subdir.getName());
				searchFiles(pcfConfigDirectory  + subdir.getName() + "\\pcf-config\\",
						targetFilesPcf);
				writeExcel("Infra\\"+subdir.getName() + "-Infra.xlsx");
			}
		}
		// searchFiles(pcfConfigDirectory, targetFilesPcf);
		// searchFiles(resourcesDirectory, targetFilesResources);

	}

	private static void searchFiles(String directory, List<String> targetFiles) throws IOException {
		try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
			paths.filter(Files::isRegularFile).filter(path -> targetFiles.contains(path.getFileName().toString()))
					.forEach(path -> {
						try {
							Map<String, Object> config = loadFile(path);
							if (config != null) {
								Map<String, Object> flatConfig = flattenMap(config, "");
								for (String key : keysToFind) {
									for (Map.Entry<String, Object> entry : flatConfig.entrySet()) {
										String configKey = entry.getKey();
										String value = entry.getValue().toString();
										if (configKey.equalsIgnoreCase(key)) {
											data.computeIfAbsent(key, k -> new ArrayList<>()).add(
													Map.of("file_name", path.getFileName().toString(), "value", value));
										} else if (configKey.contains("des.transport.iam.jwt-chain.audiences")) {
											data.computeIfAbsent(configKey, k -> new ArrayList<>()).add(
													Map.of("file_name", path.getFileName().toString(), "value", value));
										}

										if (value.contains("jdbc:oracle:thin") && jdbcOracleValues.add(value)) {
											data.computeIfAbsent("jdbc:oracle:thin", k -> new ArrayList<>())
													.add(Map.of("file_name", path.getFileName().toString(), "key",
															configKey, "value", value));
										}
									}
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
		}
	}

	private static Map<String, Object> loadFile(Path filePath) throws IOException {
		String fileName = filePath.toString();
		if (fileName.endsWith(".json")) {
			String data = "";
			try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.split(":")[0].trim().equalsIgnoreCase("\"#\"")) {
						data = data + line;
					}

				}
			} catch (IOException e) {
				System.err.println("Error reading the file: " + e.getMessage());
			}
			return new JSONObject(data).toMap();
		} else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
			return new Yaml().load(Files.newInputStream(filePath));
		} else if (fileName.endsWith(".ini") || fileName.endsWith(".config") || fileName.endsWith(".properties")) {
			try {
				Ini ini = new Ini(filePath.toFile());
				return ini.entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue())));
			} catch (InvalidFileFormatException e) {
				return Files.lines(filePath)
						.filter(line -> !line.trim().isEmpty() && !line.isBlank() && !line.trim().startsWith("#"))
						.map(line -> line.split("=", 2))
						.collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
			}
		}
		return null;
	}

	private static Map<String, Object> flattenMap(Map<String, Object> map, String parentKey) {
		Map<String, Object> flatMap = new HashMap<>();
		map.forEach((key, value) -> {
			if (!key.equalsIgnoreCase("#")) {
				String newKey = parentKey.isEmpty() ? key : parentKey + "." + key;
				if (value instanceof Map) {
					flatMap.putAll(flattenMap((Map<String, Object>) value, newKey));
				} else {
					flatMap.put(newKey, value);
				}
			}
		});
		return flatMap;
	}

	@SuppressWarnings("deprecation")
	private static void writeExcel(String fileName) throws IOException {
		try {
			String filePath = fileName;
			Workbook workbook = new XSSFWorkbook(); // Create a new workbook
			Sheet sheet = workbook.createSheet("Infra_Details");
			for (Map.Entry<String, List<Map<String, String>>> entry : data.entrySet()) {

				String key = entry.getKey();
				Set duplicateValueCheck = new HashSet();
				for (Map<String, String> map : entry.getValue()) {
					// System.out.println("Other" +map.get("file_name")+"--"+ key +"--"+
					// map.get("value"));
					String fileNamePart = "";
					String keyPart = "";
					String valuePart = "";

					if (map.get("value").contains("jdbc:oracle:thin")) {
						fileNamePart = map.get("file_name");
						keyPart = map.get("key");
						valuePart = URLDecoder.decode(map.get("value"));

					} else {
						fileNamePart = map.get("file_name");
						keyPart = key;
						valuePart = URLDecoder.decode(map.get("value"));
					}
					if (duplicateValueCheck.contains(fileNamePart+"~~"+keyPart)) {

					} else {
						try {

							// Get the first sheet or create a new one if needed

							// Find the last row and create a new row for appending
							int lastRowNum = sheet.getLastRowNum();
							Row newRow = sheet.createRow(lastRowNum + 1);

							// Add data to the new row (example data)
							newRow.createCell(0).setCellValue(fileNamePart);
							newRow.createCell(1).setCellValue(keyPart);
							newRow.createCell(2).setCellValue(valuePart);
						} catch (Exception e) {
							e.printStackTrace();
						}
						duplicateValueCheck.add(fileNamePart+"~~"+keyPart);
					}
				}
				try (FileOutputStream fos = new FileOutputStream(filePath)) {
					workbook.write(fos);
				}

			}
			System.out.println("Excel file created successfully.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
