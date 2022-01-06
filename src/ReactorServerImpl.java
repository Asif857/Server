import java.util.LinkedList;
import java.util.function.Supplier;

public class ReactorServerImpl {
    public static <T> Server<T> reactor(
            int nthreads,
            int port,
            Supplier<BidiMessagingProtocolImpl> protocolFactory,
            Supplier<MessageEncoderDecoderImpl> encoderDecoderFactory,
            LinkedList<String> filteredList
    ) {
        return new Reactor<T>(nthreads, port, protocolFactory, encoderDecoderFactory, filteredList);
    }

}

