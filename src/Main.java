public class Main {
    public static void main(String[] args) {
        HashMap<String, Integer> map = new HashMap<>();

        map.put("John", 10);
        map.put("Ivan", 20);
        map.put("Sergey", 30);

        System.out.println(map.get("John"));
        System.out.println(map.get("Ivan"));

        map.put("John", 100);

        System.out.println(map.get("John"));
        System.out.println(map.size());

        System.out.println(map.remove("Ivan"));
        System.out.println(map.get("Ivan"));
        System.out.println(map.size());
    }
}