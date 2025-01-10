package Utils;

import java.io.*;

public class InputReader {
    public static String readFormulaFromFile(String filePath) throws IOException {
        StringBuilder formula = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                formula.append(line.trim());
            }
        }
        return formula.toString();
    }
}


