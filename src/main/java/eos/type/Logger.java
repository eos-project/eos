package eos.type;

import java.util.Date;

/**
 * Logger
 */
public interface Logger extends EosEntry
{
    /**
     * Logs data with current time
     * @param data Data to log
     */
    void log(String data);

    /**
     * Logs data for provided time
     *
     * @param date Time
     * @param data Data to log
     */
    void log(Date date, String data);
}
