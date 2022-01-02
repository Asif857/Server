import java.text.ParseException;

public interface BidiMessagingProtocol<T>  {
	/**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    void start(int connectionId, Connections<T> connections);
    
    void process(T message) throws ParseException;
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
