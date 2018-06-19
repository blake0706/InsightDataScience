import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Sessionization {
    /**
     * Convert to a long integer from date time string with format "yyyy-mm-ddhh:mm:ss".
     * @param dateString date string
     * @param timeString time string
     * @return long integer value
     * @throws ParseException
     */
    private static long dateTimeToSeconds(String dateString, String timeString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-ddhh:mm:ss");
        Date date = dateFormat.parse(dateString + timeString);
        return date.getTime() / 1000;
    }

    /**
     * Write a session to output.
     * @param writer output writer
     * @param ipAddr ip address string
     * @param sessionStartDate start date string
     * @param sessionStartTime start time string
     * @param sessionEndDate end date string
     * @param sessionEndTime end time string
     * @param sessionCounter number of accesses with the session
     * @throws ParseException
     * @throws IOException
     */
    private static void writeSession(BufferedWriter writer, String ipAddr, String sessionStartDate,
                                     String sessionStartTime, String sessionEndDate,
                                     String sessionEndTime,
                                     int sessionCounter) throws ParseException, IOException {
        int duration = (int) (1 + dateTimeToSeconds(sessionEndDate, sessionEndTime)
                - dateTimeToSeconds(sessionStartDate, sessionStartTime));
        writer.write(ipAddr);
        writer.write(",");
        writer.write(sessionStartDate);
        writer.write(",");
        writer.write(sessionStartTime);
        writer.write(",");
        writer.write(sessionEndDate);
        writer.write(",");
        writer.write(sessionEndTime);
        writer.write(",");
        writer.write(String.valueOf(duration));
        writer.write(",");
        writer.write(String.valueOf(sessionCounter));
        writer.newLine();
    }

    /**
     * Main entrance to start the app.
     * @param args in the order of log file, inactivity_period and output files
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("insufficient parameters, return");
            return;
        }

        String logFile = args[0], inactivityFile = args[1], outputFile = args[2];
        BufferedReader reader = null;
        BufferedReader inactivityReader = null;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new FileReader(logFile));
            writer = new BufferedWriter(new FileWriter(outputFile));
            inactivityReader = new BufferedReader(new FileReader(inactivityFile));

            int inactivity = Integer.valueOf(inactivityReader.readLine());
            inactivityReader.close();

            Map<String, String> sessionStartDate = new HashMap<>();
            Map<String, String> sessionStartTime = new HashMap<>();
            Map<String, String> sessionEndDate = new HashMap<>();
            Map<String, String> sessionEndTime = new HashMap<>();
            Map<String, Integer> sessionCounter = new HashMap<>();

            Map<String, Integer> fieldOrder = new HashMap<>();
            Queue<String> accessingQue = new LinkedList<>();

            String oneLog = null;
            String ipAddr = null;
            String date = null;
            String time = null;

            // Read field names
            String[] fieldNames = reader.readLine().split(SEPARATOR);
            for (int i = 0; i < fieldNames.length; i++) {
                fieldOrder.put(fieldNames[i], i);
            }

            while ((oneLog = reader.readLine()) != null) {
                String[] fieldValues = oneLog.split(SEPARATOR);
                if (fieldValues.length < fieldOrder.get(TIME_STRING) + 1) {
                    System.out.println("Log with incorrect log format found, ignore.");
                    continue;
                }
                ipAddr = fieldValues[fieldOrder.get(IP_ADDRESS)];
                date = fieldValues[fieldOrder.get(DATE_STRING)];
                time = fieldValues[fieldOrder.get(TIME_STRING)];

                // Detect sessions that expired
                for (Iterator<String> iterator = accessingQue.iterator(); iterator.hasNext();) {
                    String addr = iterator.next();
                    int timePassed = (int) (dateTimeToSeconds(date, time)
                            - dateTimeToSeconds(sessionEndDate.get(addr), sessionEndTime.get(addr)));
                    if (timePassed <= inactivity) continue;

                    writeSession(writer, addr, sessionStartDate.get(addr), sessionStartTime.get(addr),
                            sessionEndDate.get(addr), sessionEndTime.get(addr), sessionCounter.get(addr));

                    iterator.remove();
                    sessionCounter.remove(addr);
                    sessionStartDate.remove(addr);
                    sessionStartTime.remove(addr);
                    sessionEndDate.remove(addr);
                    sessionEndTime.remove(addr);
                }

                // Book/update one record
                if (! sessionCounter.containsKey(ipAddr)) {
                    sessionCounter.put(ipAddr, 1);
                    sessionStartDate.put(ipAddr, date);
                    sessionStartTime.put(ipAddr, time);
                    sessionEndDate.put(ipAddr, date);
                    sessionEndTime.put(ipAddr, time);
                    accessingQue.add(ipAddr);
                } else {
                    sessionCounter.put(ipAddr, sessionCounter.get(ipAddr) + 1);
                    sessionEndDate.put(ipAddr, date);
                    sessionEndTime.put(ipAddr, time);
                }
            }

            // Flush all records in the queue
            for (String addr: accessingQue) {
                writeSession(writer, addr, sessionStartDate.get(addr), sessionStartTime.get(addr),
                        sessionEndDate.get(addr),
                        sessionEndTime.get(addr), sessionCounter.get(addr));
            }

            writer.flush();
            writer.close();
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getCause());
        } catch (IOException e) {
            System.out.println(e.getCause());
        } catch (Exception e) {
            System.out.println(e.getCause());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private static final String SEPARATOR = ",";
    private static final String IP_ADDRESS = "ip";
    private static final String DATE_STRING = "date";
    private static final String TIME_STRING = "time";
    private static final String CIK_STRING = "cik";
    private static final String ACCESSION_STRING = "accession";
    private static final String EXTENSION_STRING = "extention";
}

