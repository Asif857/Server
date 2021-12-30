import java.util.function.Supplier;

public class TPCServer{
    public static <T> Server<T> TPCServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory) {
        return new BaseServer<T>(port, protocolFactory, encoderDecoderFactory) {
            protected void execute(ConnectionHandlerImpl<T> handler) {
                new Thread(handler).start();
            }
        };
    }
    }
