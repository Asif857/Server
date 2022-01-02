import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionHandlerImpl<T> implements ConnectionHandler<T>,Runnable{
    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private User user=null;
    public ConnectionHandlerImpl(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }


    public void close() throws IOException {

    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage); // process is void - we will send the ack and the message in the process itself.
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    public void send(T msg) {
        if(msg != null) {
            try {
                BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
                out.write(encdec.encode(msg));
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BidiMessagingProtocol<T> getProtocol() {
        return protocol;
    }
    public void setUser(User user){
        this.user=user;
    }
    public User getUser(){
        return user;
    }

    public BufferedInputStream getIn() {
        return in;
    }

    public BufferedOutputStream getOut() {
        return out;
    }

    public Socket getSock() {
        return sock;
    }
}
