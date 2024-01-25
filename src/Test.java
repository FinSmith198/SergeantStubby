import java.util.Hashtable;

public class Test {
    public static void main(String[] args) {
        Hashtable<String, Integer> hashtable = new Hashtable<>();
        hashtable.put("1950", 1950);
        hashtable.put("oneseventhreefive", 1735);
        hashtable.forEach((k, o) -> System.out.println(k));
        System.out.println(hashtable);
    }
}
