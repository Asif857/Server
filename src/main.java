public class main {
    public static void main(String[] args) {
        int index =0;
        String message = "0102";
        String first = message.substring(index,2);
        index += 2;
        String second = message.substring(index,4);
        index +=2;
        System.out.println(first + " " + second);
    }
}
