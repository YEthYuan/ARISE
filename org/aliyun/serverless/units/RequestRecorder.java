package org.aliyun.serverless.units;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

public class RequestRecorder {
    public static class Request {
        protected final long timestampInMs;

        public Request(long timestampInMs) {
            this.timestampInMs = timestampInMs;
        }
    }

    private static final Logger logger = Logger.getLogger(RequestRecorder.class.getName());
    private int timeSliceInMs;
    private final int maxLength;
    private final LinkedList<Request> requests;

    public RequestRecorder(int timeSliceInMs) {
        this(timeSliceInMs, 1000);
    }

    public RequestRecorder(int timeSliceInMs, int maxLength) {
        this.timeSliceInMs = timeSliceInMs;
        this.maxLength = maxLength;
        this.requests = new LinkedList<>();
    }


    public int getTimeSliceInMs() {
        return timeSliceInMs;
    }

    public void setTimeSliceInMs(int timeSliceInMs) {
        this.timeSliceInMs = timeSliceInMs;
    }

    public void addRequest(Request request) {
        this.requests.addLast(request);
        if (this.requests.size() > maxLength) {
            this.requests.pollFirst();
        }
    }

    public int[] getRecentRequestCount(int timeSliceCount, long startTimestampInMs) {
        int[] counts = new int[timeSliceCount];
        Arrays.fill(counts, 0);
        Iterator<Request> iterator = requests.descendingIterator();
        while (iterator.hasNext()) {
            Request curRequest = iterator.next();

            if (curRequest.timestampInMs <= startTimestampInMs) {
                int sliceIndex = (int) ((startTimestampInMs - curRequest.timestampInMs) / timeSliceInMs);

//                if (sliceIndex < 0 || sliceIndex > 10000) {
//                    logger.warning(String.format("Invalid slice index: %d, ignored! Maybe integer overflow encountered!", sliceIndex));
//                    continue;
//                }

                if (sliceIndex >= timeSliceCount) {
                    break;
                } else {
                    ++counts[sliceIndex];
                }
            }
        }
        return counts;
    }
}
