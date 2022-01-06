import java.util.LinkedList;
public class ReactorMain {
    public static void main(String[] args) {
        LinkedList<String> filteredWords = new LinkedList<>();
        filteredWords.add("hello");
        filteredWords.add("asif");
        //Change nthreads and port to args[0] and args[1]
        Server server =ReactorServerImpl.reactor(10,    //change to Integer.parseInt(args[0])
                7777,                                           //change to Integer.parseInt(args[1])
                ()-> new BidiMessagingProtocolImpl(),
                ()-> new MessageEncoderDecoderImpl(),
                filteredWords
                );
        server.serve();
    }
}
