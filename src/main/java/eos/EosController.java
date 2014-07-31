package eos;

import eos.access.AccessTokenRepository;
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
     * @return Access tokens repository
     */
    AccessTokenRepository getTokenRepository();

    /**
     * Returns metrics for read and throws an exception if not found
     *
     * @param token Access token
     * @param name  Metric name
     * @return Found entry
     * @throws eos.server.WrongTokenException    If wrong or not valid token provided
     * @throws eos.server.EntryNotFoundException If entry not found
     */
    EosEntry getMetricRead(String token, EosKey name) throws WrongTokenException, EntryNotFoundException;

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
     * @param token Access token
     * @param event Event to report
     * @throws WrongTokenException If wrong or not valid token provided
     */
    void sendEvent(String token, ObservingEvent event) throws WrongTokenException;
}
