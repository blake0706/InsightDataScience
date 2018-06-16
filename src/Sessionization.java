import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Sessionization {
    /**
     *
     * @param dateString
     * @param timeString
     * @return
     * @throws ParseException
     */
    private static long dateTimeToSeconds(String dateString, String timeString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-ddhh:mm:ss");
            Date date = dateFormat.parse(dateString + timeString);
            return date.getTime() / 1000;
        } catch (ParseException e) {

        }

        return -1;
    }

    /**
     *
     * @param writer
     * @param ipAddr
     * @param sessionStartDate
     * @param sessionStartTime
     * @param sessionEndDate
     * @param sessionEndTime
     * @param sessionCounter
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
     *
     * @param args
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
            PriorityQueue<String> accessingQue = new PriorityQueue<>((ip1, ip2) -> {
                String endDate1 = sessionEndDate.get(ip1);
                String endDate2 = sessionEndDate.get(ip2);
                if (endDate1 == null || endDate2 == null) {
                    return 1;
                }
                String endTime1 = sessionEndTime.get(ip1);
                String endTime2 = sessionEndTime.get(ip2);
                long dt1 = dateTimeToSeconds(endDate1, endTime1);
                long dt2 = dateTimeToSeconds(endDate2, endTime2);
                if (dt1 != dt2) {
                    return (int) (dt1 - dt2);
                }

                String startDate1 = sessionStartDate.get(ip1);
                String startTime1 = sessionStartTime.get(ip1);
                String startDate2 = sessionStartDate.get(ip2);
                String startTime2 = sessionStartTime.get(ip2);
                return (int) (dateTimeToSeconds(startDate1, startTime1) - dateTimeToSeconds(startDate2, startTime2));
            });

            String oneLog = null;
            String ipAddr = null;
            String date = null;
            String time = null;

            // ip,date,time,zone,cik,accession,extention,code,size,idx,norefer,noagent,find,crawler,browser
            reader.readLine();
            while ((oneLog = reader.readLine()) != null) {
                String[] fieldValues = oneLog.split(",");
                if (fieldValues.length != 15) {

                }
                ipAddr = fieldValues[0];
                date = fieldValues[1];
                time = fieldValues[2];

                for (Iterator<String> iterator = accessingQue.iterator(); iterator.hasNext();) {
                    String addr = iterator.next();
                    int timePassed = (int) (dateTimeToSeconds(date, time)
                            - dateTimeToSeconds(sessionEndDate.get(addr), sessionEndTime.get(addr)));
                    if (timePassed < inactivity) {
                        break;
                    } else if (timePassed <= inactivity && addr.equals(ipAddr)) {
                        break;
                    }

                    writeSession(writer, addr, sessionStartDate.get(addr), sessionStartTime.get(addr),
                            sessionEndDate.get(addr), sessionEndTime.get(addr), sessionCounter.get(addr));

                    iterator.remove();
                    sessionCounter.remove(addr);
                    sessionStartDate.remove(addr);
                    sessionStartTime.remove(addr);
                    sessionEndDate.remove(addr);
                    sessionEndTime.remove(addr);
                }

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
                    accessingQue.remove(ipAddr);
                    accessingQue.add(ipAddr);
                }
            }

            String[] ipAddresses = accessingQue.stream().collect(Collectors.toSet()).toArray(new String[0]);
            Arrays.sort(ipAddresses, (ip1, ip2) -> {
                String startDate1 = sessionStartDate.get(ip1);
                String startTime1 = sessionStartTime.get(ip1);
                String startDate2 = sessionStartDate.get(ip2);
                String startTime2 = sessionStartTime.get(ip2);
                long dt1 = dateTimeToSeconds(startDate1, startTime1);
                long dt2 = dateTimeToSeconds(startDate2, startTime2);
                if (dt1 != dt2) {
                    return (int) (dt1 - dt2);
                }

                String endDate1 = sessionEndDate.get(ip1);
                String endDate2 = sessionEndDate.get(ip2);
                String endTime1 = sessionEndTime.get(ip1);
                String endTime2 = sessionEndTime.get(ip2);
                return (int) (dateTimeToSeconds(endDate1, endTime1) - dateTimeToSeconds(endDate2, endTime2));
            });
            for (String addr: ipAddresses) {
                writeSession(writer, addr, sessionStartDate.get(addr), sessionStartTime.get(addr),
                        sessionEndDate.get(addr),
                        sessionEndTime.get(addr), sessionCounter.get(addr));
            }

            writer.flush();
            writer.close();
            reader.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
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
}

