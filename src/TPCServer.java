import java.util.LinkedList;
import java.util.function.Supplier;

public class TPCServer{
    public static <T> Server<T> TPCServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory,
            LinkedList<String> filteredWords) {
        return new BaseServer<T>(port, protocolFactory, encoderDecoderFactory, filteredWords) {
            protected void execute(ConnectionHandlerImpl<T> handler) {
                new Thread(handler).start();
            }
        };
    }
    }
