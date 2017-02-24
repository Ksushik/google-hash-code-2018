package eu.nicolamoretto.hashchode.streaming;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author n.moretto@nicolamoretto.eu
 */
public class Main {
    
    private static int V;
    private static int E;
    private static int R;
    private static int C;
    private static int X;
    private static int[] videoSize;
    
    public static boolean isValid(int[][] matrix) {
        for (int c = 0; c < matrix.length - 1; c++) {
            int capacity = X;
            for (int v = 0; v < matrix.length && capacity > 0; v++) {
                if (matrix[c][v] == 1) {
                    capacity -= videoSize[v];
                }
            }
            if (capacity < 0) {
                return false;
            }
        }
        return true;
    }
    
    public static void main(String[] args) {
        final String delimiter = " ";
        final Path csvFile = Paths.get(System.getProperty("user.dir").concat("/input/me_at_the_zoo.in"));
        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            // Loading input data from CSV file
            // 1° line: problem parameters
            String[] firstLine = reader.readLine().split(delimiter);
            V = Integer.parseInt(firstLine[0]);
            E = Integer.parseInt(firstLine[1]);
            R = Integer.parseInt(firstLine[2]);
            C = Integer.parseInt(firstLine[3]);
            X = Integer.parseInt(firstLine[4]);
            
            String[] secondLine = reader.readLine().split(delimiter);
            videoSize = new int[V];
            for (int i = 0; i < videoSize.length; i++) {
                videoSize[i] = Integer.parseInt(secondLine[i]);
            }
            
            final int[][] latencies = new int[C+1][E];
            for (int e = 0; e < E; e++) {
                String[] line = reader.readLine().split(delimiter);
                latencies[C][e] = Integer.parseInt(line[0]);
                final int connectedCacheServers = Integer.parseInt(line[1]);
                for (int c = 0; c < connectedCacheServers; c++) {
                    line = reader.readLine().split(delimiter);
                    int cacheId = Integer.parseInt(line[0]);
                    latencies[cacheId][e] = Integer.parseInt(line[1]);
                }
            }
            
            final int[][] requests = new int[V][E];
            for (int r = 0; r < R; r++) {
                String[] line = reader.readLine().split(delimiter);
                int video = Integer.parseInt(line[0]);
                int endpoint = Integer.parseInt(line[1]);
                requests[video][endpoint] = Integer.parseInt(line[2]);
            }
            
            // Assign -1 to each cell where there are
            // no requests for the video from any endpoint
            // connected to the cache server
            int[][] allocation = new int[C+1][V];
            for (int c = 0; c < C; c++) {
                for (int v = 0; v < V; v++) {
                    // Should I put video v into cache server c?
                    int[] endpoints = latencies[c];
                    int totalRequests = 0;
                    // Count total requests for video v from endpoints connected to cache server c
                    for (int e = 0; e < endpoints.length; e++) {
                        if (latencies[c][e] > 0) {
                            // Cache server c is connected to endpoint e
                            totalRequests += requests[v][e];
                        }
                    }
                    if (totalRequests == 0) {
                        allocation[c][v] = -1;
                    }
                }
            }
            
            int usedCacheServers = 0;
            for (int c = 0; c < C; c++) {
                int capacity = X;
                for (int v = 0; v < V && capacity > 0; v++) {
                    allocation[c][v] = 1;
                    capacity -= videoSize[v];
                }
                if (capacity < X) {
                    usedCacheServers++;
                }
            }
            
            final Path csvResults = Paths.get(System.getProperty("user.dir").concat("/output/me_at_the_zoo.csv"));
            try(BufferedWriter writer = Files.newBufferedWriter(csvResults, StandardOpenOption.CREATE)) {
                writer.write(String.format("%s%s", usedCacheServers, System.lineSeparator()));
                for (int c = 0; c < C; c++) {
                    StringBuilder builder = new StringBuilder(String.valueOf(c));
                    for (int v = 0; v < V; v++) {
                        if (allocation[c][v] == 1) {
                            builder.append(" ");
                            builder.append(v);
                        }
                    }
                    String row = builder.toString().trim().concat(System.lineSeparator());
                    writer.write(row);
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
