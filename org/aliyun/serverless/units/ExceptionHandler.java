package org.aliyun.serverless.units;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler {

    //1、
    public static String getTrace(Throwable t) {
        StringWriter stringWriter= new StringWriter();
        PrintWriter writer= new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        StringBuffer buffer= stringWriter.getBuffer();
        return buffer.toString();
    }

    //2、
    public static String getExceptionAllinformation(Exception ex){
        String sOut = "";
        StackTraceElement[] trace = ex.getStackTrace();
        for (StackTraceElement s : trace) {
            sOut += "\tat " + s + "\r\n";
        }
        return sOut;
    }

    public static String all(Exception e) {
        StringBuffer sb = new StringBuffer();
        sb.append("Error Message: ").append(e.getMessage()).append('\n')
                .append("Stack Trace:").append(getTrace(e)).append("\n")
                .append("Exception:").append(getExceptionAllinformation(e));

        return sb.toString();
    }

//    //3、
//    public static String getExceptionAllinformation_01(Exception ex) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PrintStream pout = new PrintStream(out);
//        ex.printStackTrace(pout);
//        String ret = new String(out.toByteArray());
//        pout.close();
//        try {
//            out.close();
//        } catch (Exception e) {
//        }
//        return ret;
//    }

    //4、
    private static String toString_02(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
