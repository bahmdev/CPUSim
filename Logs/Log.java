package cpusim.logs;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 *
 * @author Bilal Ahmad & Mathew Levine
 * Date: 12/06/13
 *
 */
public class Log {

    //log verbosity
    public static final int NONE = 0;       //none
    public static final int DEBUG = 1;      //diagnostics
    public static final int WARNING = 2;    //might cause problem
    public static final int ERROR = 3;      //major problem
    public static final int CRITICAL = 4;   //fatal problem


    private static int level; //current verbosity
    private static Logger logger;

    /**
     * Sets the verbosity of the logger
     * @param level verbosity
     */
    public static void setLevel(int level){
        Log.level = level;
        logger = new Logger();
    }

    //log inputs

    public static void debug(String message){
        if (level>=DEBUG) logger.log(DEBUG, message, null);
    }

    public static void debug(String message, Throwable e){
        if (level>=DEBUG) logger.log(DEBUG, message, e);
    }

    public static void warning(String message){
        if (level>=WARNING) logger.log(WARNING, message, null);
    }

    public static void WARNING(String message, Throwable e){
        if (level>=WARNING) logger.log(WARNING, message, e);
    }

    public static void error(String message){
        if (level>=ERROR) logger.log(ERROR, message, null);
    }

    public static void error(String message, Throwable e){
        if (level>=ERROR) logger.log(ERROR, message, e);
    }

    public static void critical(String message){
        if (level>=CRITICAL) logger.log(CRITICAL, message, null);
    }

    public static void critical(String message, Throwable e){
        if (level>=CRITICAL) logger.log(CRITICAL, message, e);
    }


    /**
     * Logger is used to execute the logs
     */
    private static class Logger{

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


        /**
         * log is called from Log class
         * @param level verbosity
         * @param report message
         * @param e exception
         */
        public void log(int level, String report, Throwable e){

            StringBuilder builder = new StringBuilder();
            Date date = new Date();
            builder.append(dateFormat.format(date)+"\n");

            if (level == Log.CRITICAL)
                builder.append("CRITICAL ERROR: ");
            else if (level == Log.ERROR)
                builder.append("ERROR: ");
            else if (level == Log.WARNING)
                builder.append("WARNING: ");
            else if (level == Log.DEBUG)
                builder.append("DEBUG: ");
            else
                builder.append("OTHER: ");

            builder.append(report);

            if (e != null){
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                builder.append(sw.toString());
            }
            String url = Log.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(url.substring(5)+ "cpusim/logs/log.txt", true)));
                out.println("\n"+builder.toString());
                out.close();
            } catch (IOException ieo) {
                System.out.print("FAILED TO UPDATE ERROR LOG");
            }
        }
    }





}
