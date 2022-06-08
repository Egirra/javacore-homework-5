import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Задача 1: CSV - JSON парсер
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileNameCsv = "data.csv";
        String fileNameJson = "data.json";
        List<Employee> listFromCsv = parseCSV(columnMapping, fileNameCsv);
        String jsonFromCsv = listToJson(listFromCsv);
        writeString(jsonFromCsv, fileNameJson);

        // Задача 2: XML - JSON парсер
        String fileNameJson2 = "data2.json";
        String fileNameXml = "data.xml";
        List<Employee> listFromXml = parseXML(fileNameXml);
        String jsonFromXml = listToJson(listFromXml);
        writeString(jsonFromXml, fileNameJson2);

        // Задача 3: JSON парсер
        String jsonFileName = "data.json";
        String json = readString(jsonFileName);
        List<Employee> list = jsonToList(json);
        list.forEach(System.out::println);
    }

    private static List<Employee> parseCSV (String[] columnMapping, String fileNameCsv) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileNameCsv))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            List<Employee> staff = csv.parse();
            return staff;
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<Employee>();
        }
    }

    private static String listToJson (List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        return gson.toJson(list, listType);
    }

    private static void writeString(String jsonFromCsv, String fileNameJson) {
        try (FileWriter writer = new FileWriter(fileNameJson)) {
            writer.write(jsonFromCsv);
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static List<Employee> parseXML (String fileNameXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileNameXml));
            Node root = doc.getDocumentElement();
            ArrayList<Employee> employees = new ArrayList<Employee>();
            read(root, employees);
            return employees;
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            ex.printStackTrace();
            return new ArrayList<Employee>();
        }
    }

    private static void read (Node root, ArrayList<Employee> employees) {
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                Element element = (Element) node;
                employees.add(new Employee(
                        Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent()),
                        element.getElementsByTagName("firstName").item(0).getTextContent(),
                        element.getElementsByTagName("lastName").item(0).getTextContent(),
                        element.getElementsByTagName("country").item(0).getTextContent(),
                        Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent())));
            }
        }
    }

    private static String readString (String jsonFileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(jsonFileName))) {
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }
        } catch (IOException ex) {
            ex.getMessage();
        }
        return sb.toString();
    }

    public static List<Employee> jsonToList (String json) {
        List<Employee> employees = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            Object parser = new JSONParser().parse(json);
            JSONArray jsonArray = (JSONArray) parser;
            for (int i = 0; i < jsonArray.size(); i++) {
                Employee employee = gson.fromJson(jsonArray.get(i).toString(), Employee.class);
                employees.add(employee);
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return employees;
    }
}