package eos;

import eos.observers.ObservingEvent;
import eos.server.EntryNotFoundException;
import eos.server.WrongTokenException;
import eos.type.EosEntry;
import eos.type.EosKey;

import java.util.List;

/**
 * Controller for EOS service
 */
public interface EosController
{
    /**
     * Returns metrics for read and throws an exception if not found
     *
     * @param name  Metric name
     * @return Found entry
     * @throws eos.server.EntryNotFoundException If entry not found
     */
    EosEntry getMetricRead(EosKey name) throws WrongTokenException, EntryNotFoundException;

    /**
     * Returns list of metric names, that matches a pattern
     *
     * @param pattern Pattern to match
     * @return List of names
     */
    List<String> findMetrics(String pattern);

    /**
     * Sends update event to metric registry
     *
     * @param event Event to report
     * @throws WrongTokenException If wrong or not valid token provided
     */
    void sendEvent(ObservingEvent event) throws WrongTokenException;
}
