import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {

    public static void main(String[] args) {
        try {
            // Load and parse JSON input
            File file = new File("../testcases.json"); // Ensure the file path is correct
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(file);

            // Extract the "keys" node
            JsonNode keysNode = rootNode.get("keys");
            int n = keysNode.get("n").asInt();
            int k = keysNode.get("k").asInt();

            // Calculate degree of polynomial
            int m = k - 1;

            // Parse points (x, y) from JSON
            List<Point> points = new ArrayList<>();
            rootNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                if (key.matches("\\d+")) { // Check if key is numeric
                    int x = Integer.parseInt(key);
                    JsonNode pointNode = entry.getValue();
                    int base = pointNode.get("base").asInt();
                    String value = pointNode.get("value").asText();
                    BigInteger y = new BigInteger(value, base);
                    points.add(new Point(x, y));
                }
            });

            if (points.size() < k) {
                System.out.println("Not enough points for interpolation.");
                return;
            }

            // Calculate the constant term (secret) using Lagrange Interpolation
            BigInteger secret = calculateSecret(points.subList(0, k));
            System.out.println("Secret (constant term c) is: " + secret);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Point {
        int x;
        BigInteger y;

        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static BigInteger calculateSecret(List<Point> points) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            BigInteger term = points.get(i).y;

            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    int xi = points.get(i).x;
                    int xj = points.get(j).x;

                    BigInteger numerator = BigInteger.valueOf(-xj);
                    BigInteger denominator = BigInteger.valueOf(xi - xj);

                    term = term.multiply(numerator).divide(denominator);
                }
            }

            result = result.add(term);
        }

        return result;
    }
}
