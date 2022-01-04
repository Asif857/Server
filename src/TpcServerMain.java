import java.util.LinkedList;

public class TpcServerMain {
    public static void main(String[] args) {
        LinkedList<String> filteredWords = new LinkedList<>();
        filteredWords.add("hello");
        filteredWords.add("asif");
        Server server = TPCServer.TPCServer(7777,
                ()-> new BidiMessagingProtocolImpl(),
                ()-> new MessageEncoderDecoderImpl(),
                filteredWords);
        server.serve();
    }
}
