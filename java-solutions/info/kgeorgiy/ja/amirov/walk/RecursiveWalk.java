package info.kgeorgiy.ja.amirov.walk;

public class RecursiveWalk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Wrong args");
        } else {
            try {
                Walk.walk(args[0], args[1], Integer.MAX_VALUE);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

