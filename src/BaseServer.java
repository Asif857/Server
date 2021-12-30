import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;
public abstract class BaseServer<T> implements Server<T> {
    private final ConnectionsImpl<T> connections;
    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    private int connectId=0;

    public BaseServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {
        this.connections = new ConnectionsImpl<>();
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
    }

    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();
                ConnectionHandlerImpl<T> handler = new ConnectionHandlerImpl(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get());
                connections.addHandlerMap(connectId,handler);
                handler.getProtocol().start(connectId,connections);
                connectId++;
                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(ConnectionHandlerImpl<T> handler);

}
